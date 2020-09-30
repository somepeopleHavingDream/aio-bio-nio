package org.yangxin.aio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

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
    private static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Object o) {

        }

        @Override
        public void failed(Throwable throwable, Object o) {

        }
    }
}
