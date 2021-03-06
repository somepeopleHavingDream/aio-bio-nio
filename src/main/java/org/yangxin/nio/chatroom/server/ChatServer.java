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

    public ChatServer(int PORT) {
        this.PORT = PORT;
    }

    /**
     * 服务端主流程
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void start() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(PORT));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + PORT + "……");

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeySet) {
                    // 处理被触发的事件
                    handles(selectionKey);
                }
                selectionKeySet.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey selectionKey) throws IOException {
        // ACCEPT事件：和客户端建立了连接
        if (selectionKey.isAcceptable()) {
            handlesAcceptable(selectionKey);
        } else if (selectionKey.isReadable()) {
            // READ事件：客户端发送了消息
            handlesReadable(selectionKey);
        }
    }

    private void handlesReadable(SelectionKey selectionKey) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        String forwardMsg = receive(client);
        if (forwardMsg == null || forwardMsg.isEmpty()) {
            // 客户端异常
            selectionKey.cancel();
            selector.wakeup();
        } else {
            System.out.println(getClientName(client) + "：" + forwardMsg);
            forwardMessage(client, forwardMsg);

            // 检查用户是否退出
            if (readyToQuit(forwardMsg)) {
                selectionKey.cancel();
                selector.wakeup();
                System.out.println(getClientName(client) + "已断开。");
            }
        }
    }

    private void handlesAcceptable(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println(getClientName(client) + "已连接。");
    }

    private String getClientName(SocketChannel client) {
        return "客户端【" + client.socket().getPort() + "】";
    }

    private void forwardMessage(SocketChannel client, String forwardMsg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel connectedClient = (SocketChannel) key.channel();
                if (!client.equals(connectedClient)) {
                    WRITER_BUFFER.clear();
                    WRITER_BUFFER.put(CHARSET.encode(getClientName(client) + ": " + forwardMsg));
                    WRITER_BUFFER.flip();
                    while (WRITER_BUFFER.hasRemaining()) {
                        connectedClient.write(WRITER_BUFFER);
                    }
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private String receive(SocketChannel client) throws IOException {
        READ_BUFFER.clear();
        while (client.read(READ_BUFFER) > 0);
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
