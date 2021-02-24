package org.yangxin.bio.chatroom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/09/24 16:48
 */
public class ChatHandler implements Runnable {

    private final ChatServer server;
    private final Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 存储新上线的用户
            server.addClient(socket);

            // 读取用户发送的消息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = reader.readLine()) != null) {
                String forwardMsg = "客户端【" + socket.getPort() + "】：" + msg + "\n";
                System.out.print(forwardMsg);

                // 将消息转发给聊天室里在线的其他用户
                server.forwardMessage(socket, forwardMsg);

                // 检查用户是否准备退出
                if (server.readyToQuit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
