package org.yangxin.nio.chatroom.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author yangxin
 * 2020/09/27 21:15
 */
public class ChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private Selector selector;
    private final ByteBuffer READ_BUFFER = ByteBuffer.allocate(BUFFER);
    private final ByteBuffer WRITER_BUFFER = ByteBuffer.allocate(BUFFER);
    private final Charset CHARSET = StandardCharsets.UTF_8;
    private final int PORT;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.PORT = port;
    }

    /**
     * 服务端主流程
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void start() {
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(PORT));

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + PORT + "……");

            while (true) {
                // select()阻塞到至少有一个通道在你注册的事件上就绪了
                selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();
                for (SelectionKey key : keySet) {
                    // 处理被触发的事件
                    handles(key);
                }
                keySet.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        // ACCEPT事件：和客户端建立了连接
        if (key.isAcceptable()) {
            handlesAcceptable(key);
        } else if (key.isReadable()) {
            // READ事件：客户端发送了消息
            handlesReadable(key);
        }
    }

    private void handlesReadable(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        String forwardMsg = receive(channel);
        if (forwardMsg == null || forwardMsg.isEmpty()) {
            // 客户端异常
            key.cancel();
            // wakeup用于唤醒阻塞在select方法上的线程
            selector.wakeup();
        } else {
            System.out.println(getClientName(channel) + "：" + forwardMsg);
            forwardMessage(channel, forwardMsg);

            // 检查用户是否退出
            if (readyToQuit(forwardMsg)) {
                key.cancel();
                selector.wakeup();
                System.out.println(getClientName(channel) + "已断开。");
            }
        }
    }

    private void handlesAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        System.out.println(getClientName(clientChannel) + "已连接。");
    }

    private String getClientName(SocketChannel client) {
        return "客户端【" + client.socket().getPort() + "】";
    }

    private void forwardMessage(SocketChannel channel, String msg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel connectedChannel = (SocketChannel) key.channel();
                if (!channel.equals(connectedChannel)) {
                    WRITER_BUFFER.clear();
                    WRITER_BUFFER.put(CHARSET.encode(getClientName(channel) + ": " + msg));
                    WRITER_BUFFER.flip();
                    while (WRITER_BUFFER.hasRemaining()) {
                        connectedChannel.write(WRITER_BUFFER);
                    }
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private String receive(SocketChannel channel) throws IOException {
        READ_BUFFER.clear();
        while (channel.read(READ_BUFFER) > 0) {
        }
        READ_BUFFER.flip();
        return String.valueOf(CHARSET.decode(READ_BUFFER));
    }

    public boolean readyToQuit(String msg) {
        return  QUIT.equals(msg);
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

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(7777);
        chatServer.start();
    }
}
