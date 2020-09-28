package org.yangxin.nio.chatroom.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author yangxin
 * 2020/09/28 19:25
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ChatClient {

    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private final String HOST;
    private final int PORT;
    private SocketChannel client;
    private final ByteBuffer READ_BUFFER = ByteBuffer.allocate(BUFFER);
    private final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(BUFFER);
    private Selector selector;
    private final Charset CHARSET = StandardCharsets.UTF_8;

    public ChatClient() {
        this(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
    }

    public ChatClient(String HOST, int PORT) {
        this.HOST = HOST;
        this.PORT = PORT;
    }

    public boolean readyToQuit(String msg) {
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

    @SuppressWarnings("InfiniteLoopStatement")
    private void start() {
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);

            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            client.connect(new InetSocketAddress(HOST, PORT));

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    handles(key);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            // 用户正常退出
        } finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        // CONNECT事件：连接就绪事件
        if (key.isConnectable()) {
            handlesConnectable(key);
        } else if (key.isReadable()) {
            // READ事件：服务器转发消息
            handlesReadable(key);
        }
    }

    private void handlesReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        String msg = receive(client);
        if (msg == null || msg.isEmpty()) {
            // 服务器异常
            close(selector);
        } else {
            System.out.println(msg);
        }
    }

    private void handlesConnectable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        if (client.isConnectionPending()) {
            client.finishConnect();

            // 处理用户的输入
            new Thread(new UserInputHandler(this)).start();
        }
        client.register(selector, SelectionKey.OP_READ);
    }

    private String receive(SocketChannel client) throws IOException {
        READ_BUFFER.clear();
        while (client.read(READ_BUFFER) > 0);
        READ_BUFFER.flip();
        return String.valueOf(CHARSET.decode(READ_BUFFER));
    }

    public void send(String msg) throws IOException {
        if (msg == null || msg.isEmpty()) {
            return;
        }

        WRITE_BUFFER.clear();
        WRITE_BUFFER.put(CHARSET.encode(msg));
        WRITE_BUFFER.flip();
        while (WRITE_BUFFER.hasRemaining()) {
            client.write(WRITE_BUFFER);
        }

        // 检查用户是否准备退出
        if (readyToQuit(msg)) {
            close(selector);
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1", 7777);
        client.start();
    }
}
