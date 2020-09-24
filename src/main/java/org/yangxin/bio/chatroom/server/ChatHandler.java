package org.yangxin.bio.chatroom.server;

import java.io.IOException;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/09/24 16:48
 */
public class ChatHandler implements Runnable {

    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void run() {
        try {
            server.addClient(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
