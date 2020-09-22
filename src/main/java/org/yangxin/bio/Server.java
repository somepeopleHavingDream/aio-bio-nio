package org.yangxin.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/09/22 20:19
 */
public class Server {

    public static void main(String[] args) {
        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;

        try {
            // 绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口" + DEFAULT_PORT);

            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端【" + socket.getPort() + "】已连接");

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // 读取客户端发送的消息
                String msg = reader.readLine();
                if (msg != null) {
                    System.out.println("客户端【" + socket.getPort() + "】：" + msg);

                    // 回复客户发送的消息
                    writer.write("服务器：" + msg + "\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
