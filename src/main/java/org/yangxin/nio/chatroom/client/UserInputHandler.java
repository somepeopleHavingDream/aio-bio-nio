package org.yangxin.nio.chatroom.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author yangxin
 * 2020/09/24 16:47
 */
@SuppressWarnings("DuplicatedCode")
public class UserInputHandler implements Runnable {

    private final ChatClient client;

    public UserInputHandler(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            // 等待用户输入消息
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();

                // 向服务器发送消息
                client.send(input);

                // 检查用户是否准备退出
                if (client.readyToQuit(input)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
