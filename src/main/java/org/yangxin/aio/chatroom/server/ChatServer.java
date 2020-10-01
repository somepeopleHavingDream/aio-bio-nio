package org.yangxin.aio.chatroom.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yangxin
 * 2020/10/01 13:34
 */
public class ChatServer {

    private static final String LOCALHOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;
    private static final int THREAD_POOL_SIZE = 8;

    private AsynchronousServerSocketChannel serverChannel;
    private final List<ClientHandler> connectedClientList;
    private final Charset charset = StandardCharsets.UTF_8;
    private final int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
        this.connectedClientList = new ArrayList<>();
    }

    private boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "InfiniteLoopStatement"})
    private void start() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverChannel.bind(new InetSocketAddress(LOCALHOST, port));
            System.out.println("启动服务器，监听端口：" + port);

            while (true) {
                serverChannel.accept(null, new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }
    }

    /**
     * @author yangxin
     * 2020/10/01 16:48
     */
    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object o) {
            if (serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }
            if (clientChannel != null && clientChannel.isOpen()) {
                ClientHandler handler = new ClientHandler(clientChannel);
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
                // 将新用户添加到在线用户列表
                addClient(handler);
                clientChannel.read(buffer, buffer, handler);
            }
        }

        @Override
        public void failed(Throwable throwable, Object o) {
            System.out.println("连接失败：" + throwable);
        }
    }

    private synchronized void addClient(ClientHandler handler) {
        connectedClientList.add(handler);
        System.out.println(getClientName(handler.clientChannel) + "已连接到服务器");
    }

    private synchronized void removeClient(ClientHandler handler) {
        connectedClientList.remove(handler);
        System.out.println(getClientName(handler.clientChannel) + "已断开连接");
        close(handler.clientChannel);
    }

    /**
     * @author yangxin
     * 2020/10/01 17:10
     */
    private class ClientHandler implements CompletionHandler<Integer, Object> {

        private final AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            if (buffer != null) {
                if (result <= 0) {
                    // 客户端异常
                    // 将客户端移除在线客户列表
                    removeClient(this);
                } else {
                    buffer.flip();
                    String forwardMsg = receive(buffer);
                    System.out.println(getClientName(clientChannel) + ": " + forwardMsg);
                    forwardMessage(clientChannel, forwardMsg);
                    buffer.clear();

                    // 检查用户是否退出
                    if (readyToQuit(forwardMsg)) {
                        removeClient(this);
                    } else {
                        clientChannel.read(buffer, buffer, this);
                    }
                }
            }
        }

        @Override
        public void failed(Throwable throwable, Object o) {
            System.out.println("读写失败：" + throwable);
        }
    }

    private synchronized void forwardMessage(AsynchronousSocketChannel clientChannel, String forwardMsg) {
        for (ClientHandler handler : connectedClientList) {
            if (!clientChannel.equals(handler.clientChannel)) {
                try {
                    ByteBuffer buffer = charset.encode(getClientName(handler.clientChannel) + ": " + forwardMsg);
                    handler.clientChannel.write(buffer, null, handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getClientName(AsynchronousSocketChannel clientChannel) {
        int clientPort = -1;
        try {
            InetSocketAddress address = (InetSocketAddress) clientChannel.getRemoteAddress();
            clientPort = address.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "客户端【" + clientPort + "】";
    }

    private String receive(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);
        return String.valueOf(charBuffer);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(7777);
        server.start();
    }
}
