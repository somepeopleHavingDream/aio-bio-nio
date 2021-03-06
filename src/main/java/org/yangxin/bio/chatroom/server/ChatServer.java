package org.yangxin.bio.chatroom.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yangxin
 * 2020/09/24 16:48
 */
@SuppressWarnings({"InfiniteLoopStatement", "AlibabaThreadPoolCreation"})
public class ChatServer {

    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private final Map<Integer, Writer> writerByPort;

    public ChatServer() {
        executorService = Executors.newFixedThreadPool(10);
        writerByPort = new HashMap<>();
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if (socket == null) {
            return;
        }

        int port = socket.getPort();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        writerByPort.put(port, writer);

        System.out.println("客户端【" + port + "】已连接到服务器");
    }

    /**
     * 移除对应客户端套接字
     *
     * @param socket 客户端套接字
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket == null) {
            return;
        }

        int port = socket.getPort();
        if (writerByPort.containsKey(port)) {
            // 套接字关闭，客户端那边会通过isInputShutdown知道该套接字已经被关闭，从而继续客户端下一步的业务流程
            writerByPort.get(port).close();
        }
        writerByPort.remove(port);

        System.out.println("客户端【" + port + "】已断开连接");
    }

    public synchronized void forwardMessage(Socket socket, String forwardMsg) throws IOException {
        for (Map.Entry<Integer, Writer> entry : writerByPort.entrySet()) {
            if (!Objects.equals(entry.getKey(), socket.getPort())) {
                Writer writer = entry.getValue();
                writer.write(forwardMsg);
                writer.flush();
            }
        }
    }

    /**
     * 服务端主流程
     */
    public void start() {
        try {
            // 绑定监听端口
            int defaultPort = 8888;
            serverSocket = new ServerSocket(defaultPort);
            System.out.println("启动服务器，监听端口：" + defaultPort);

            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建ChatHandler线程
                executorService.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public boolean readyToQuit(String msg) {
        String quit = "quit";
        return  quit.equals(msg);
    }

    public synchronized void close() {
        if (serverSocket != null)  {
            try {
                serverSocket.close();
                System.out.println("关闭serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }
}
