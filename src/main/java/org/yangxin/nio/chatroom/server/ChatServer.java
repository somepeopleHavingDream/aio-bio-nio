package org.yangxin.nio.chatroom.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Set;

/**
 * @author yangxin
 * 2020/09/27 21:15
 */
public class ChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    private Selector selector;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER);
    private final Charset charset = StandardCharsets.UTF_8;
    private final int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    /**
     * 服务端主流程
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void start() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + port + "……");

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

    private void handles(SelectionKey selectionKey) {
        // ACCEPT事件：和客户端建立了连接
        if (selectionKey.isAcceptable()) {

        }
        // READ事件：客户端发送了消息
        if (selectionKey.isReadable()) {

        }
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
    }
}
