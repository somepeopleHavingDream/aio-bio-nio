package org.yangxin.bio;

import java.io.*;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/09/22 20:47
 */
public class Client {

    public static void main(String[] args) {
        final String quit = "quit";
        final String defaultServerHost = "127.0.0.1";
        final int defaultServerPort = 8888;
        Socket socket;
        BufferedWriter writer = null;

        try {
            // 创建socket
            socket = new Socket(defaultServerHost, defaultServerPort);

            // 创建IO流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 等待用户输入信息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();

                // 发送消息给服务器
                writer.write(input + "\n");
                writer.flush();

                // 读取服务器返回的消息
                String msg = reader.readLine();
                System.out.println(msg);

                // 查看用户是否退出
                if (quit.equals(input)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    System.out.println("关闭socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
