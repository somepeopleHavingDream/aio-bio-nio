package org.yangxin.bio.chatroom.client;

import java.io.*;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/09/24 16:47
 */
public class ChatClient {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 发送消息给服务器
     */
    public void send(String msg) throws IOException {
        if (socket.isOutputShutdown()) {
            return;
        }

        writer.write(msg + "\n");
        writer.flush();
    }

    /**
     * 从服务器接收消息
     */
    public String receive() throws IOException {
        if (socket.isInputShutdown()) {
            return null;
        }

        return reader.readLine();
    }

    /**
     * 检查用户是否准备退出
     */
    public boolean readyToQuit(String msg) {
        final String QUIT = "quit";
        return QUIT.equals(msg);
    }

    public void start() {
        try {
            // 创建socket
            final String DEFAULT_SERVER_HOST = "127.0.0.1";
            final int DEFAULT_SERVER_PORT = 8888;
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            // 创建IO流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 处理用户的输入
            new Thread(new UserInputHandler(this)).start();
            // 读取服务器转发的消息
            String msg;
            while ((msg = receive()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        if (writer == null) {
            return;
        }

        System.out.println("关闭socket");
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }
}
