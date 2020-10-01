package org.yangxin.aio.chatroom;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author yangxin
 * 2020/10/01 17:59
 */
public class ChatClient {

    private static final String LOCALHOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private String host;
    private int port;
    private AsynchronousSocketChannel clientChannel;
    private Charset charset = StandardCharsets.UTF_8;

    public ChatClient() {
        this(LOCALHOST, DEFAULT_PORT);
    }

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
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

    private void start() {
        try {
            // 创建channel
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(host, port));
            future.get();

            // 处理用户的输入
            new Thread(new UserInputHandler(this)).start();

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
            while (true) {
                Future<Integer> resultResult = clientChannel.read(buffer);
                int result = resultResult.get();
                if (result <= 0) {
                    // 服务器异常
                    System.out.println("服务器断开");
                    close(clientChannel);
                } else {
                    buffer.flip();
                    String msg = String.valueOf(charset.decode(buffer));
                    buffer.clear();
                    System.out.println(msg);
                }
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {

    }

    public static void main(String[] args) {

    }
}
