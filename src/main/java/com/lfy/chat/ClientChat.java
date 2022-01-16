package com.lfy.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientChat extends JFrame {

    // 显示文本的界面框
    private JTextArea ta = new JTextArea(10, 20);
    // 输入内容的文本框
    private JTextField tf = new JTextField(20);

    private static final String CONNIP = "127.0.0.1";
    private static final int CONNPORT = 8888;

    private Socket s = null;
    private DataOutputStream dos = null;

    private boolean isConn = false;

    // 支持滚动条的界面
    private JScrollPane sp = new JScrollPane(ta);

    public ClientChat() throws HeadlessException {
        super();
    }

    public void init() {
        this.setTitle("Client Window");
        this.add(sp, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);
        this.setBounds(300, 300, 300, 400);

        // 监听回车键
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String strSent = tf.getText();

                if (strSent.trim().length() == 0) {
                    return;
                }

                send(strSent);

                // 点击回车后，将输入框清空
                tf.setText("");
//                // 将内容添加到输出框
//                ta.append(textSent + "\n");
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 显示文本的区域不能编辑
        ta.setEditable(false);
        // 光标聚集
        tf.requestFocus();

        try {
            s = new Socket(CONNIP, CONNPORT);
            isConn = true;
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        this.setVisible(true);

        // 接收消息的线程
        new Thread(new Receive()).start();
    }

    /**
     * 发数据给服务端
     */
    public void send(String str) {
        try {
            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开一个线程，接收从服务器端传来的消息
     */
    class Receive implements Runnable {
        @Override
        public void run() {
            try {
                // 此处在通信环境下也是一个while true循环
                while (isConn) {
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    String strReceive = dis.readUTF();
                    ta.append(strReceive);
                }
            } catch (SocketException e1) {
                System.out.println("Server has been suspended");
                ta.append("Server has been suspended");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ClientChat clientChat = new ClientChat();
        clientChat.init();
    }
}
