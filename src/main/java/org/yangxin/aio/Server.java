package org.yangxin.aio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangxin
 * 2020/09/30 13:40
 */
public class Server {

    private final String LOCALHOST = "localhost";
    private final int DEFAULT_PORT = 8888;
    private AsynchronousServerSocketChannel serverChannel;

    private void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
            System.out.println("关闭" + closeable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void start() {
        try {
            // 绑定监听端口
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);

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
     * 2020/09/30 14:51
     */
    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Object o) {
            if (serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }

            AsynchronousSocketChannel clientChannel = asynchronousSocketChannel;
            if (clientChannel != null && clientChannel.isOpen()) {
                ClientHandler handler = new ClientHandler(clientChannel);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Map<String, Object> info = new HashMap<>();
                info.put("type", "read");
                info.put("buffer", buffer);

                clientChannel.read(buffer, info, handler);
            }
        }

        @Override
        public void failed(Throwable throwable, Object o) {
            // 处理错误
        }
    }

    /**
     * @author yangxin
     * 2020/09/30 17:38
     */
    private class ClientHandler implements CompletionHandler<Integer, Object> {

        private final AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel channel) {
            this.clientChannel = channel;
        }

        @Override
        public void completed(Integer integer, Object o) {
            Map<String, Object> info = (Map<String, Object>) o;
            String type = (String) info.get("type");

            if ("read".equals(type)) {
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                buffer.flip();
                info.put("type", "write");
                clientChannel.write(buffer, info, this);
            }
        }

        @Override
        public void failed(Throwable throwable, Object o) {

        }
    }
}
