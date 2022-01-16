package com.lfy.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerChat extends JFrame {

    private static final int PORT = 8888;
    JTextArea serverTa = new JTextArea();
    private JPanel btnTool = new JPanel();
    private JButton startBtn = new JButton("Start");
    private JButton stopBtn = new JButton("Stop");

    private static ServerSocket serverSocket = null;

    // 一个维护多个客户端连接的列表
    private ArrayList<ClientConn> clientConns = new ArrayList<ClientConn>();

    private boolean isStart = false;

    // 支持滚动条的界面
    private JScrollPane sp = new JScrollPane(serverTa);

    public ServerChat() {
        this.setTitle("Server");
        this.add(sp, BorderLayout.CENTER);

        btnTool.add(startBtn);
        btnTool.add(stopBtn);
        this.add(btnTool, BorderLayout.SOUTH);

        this.setBounds(0, 0, 500, 500);

        if (isStart) {
            serverTa.append("Server began\n");
        } else {
            serverTa.append("Server did not begin yet\n");
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isStart = false;
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                    serverTa.append("Server disconnect");
                    System.exit(0);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        stopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        isStart = false;
                    }
                    System.exit(0);
                    serverTa.append("Server disconnect\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        // 该监听按键暂时无用
        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket == null) {
                        serverSocket = new ServerSocket(PORT);
                    }
                    isStart = true;
                    serverTa.append("Server began\n");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        this.setVisible(true);
        startServer();
    }

    public void startServer() {
        try {
            try {
                serverSocket = new ServerSocket(PORT);
                isStart = true;
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            serverTa.append("Server has been started\n");
            while (isStart) {
                Socket socket = serverSocket.accept();
                clientConns.add(new ClientConn(socket));
                serverTa.append("a client has connected to server: " + socket.getInetAddress()
                        + "/" + socket.getPort() + "\n");
            }
        } catch (SocketException e1) {
            System.out.println("Server has been suspended\n");
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /**
     * 服务器端的一个连接对象
     */
    class ClientConn implements Runnable {
        Socket s = null;
        public ClientConn(Socket s) {
            this.s = s;
            (new Thread(this)).start();
        }

        /**
         * 多线程接收客户端数据
         */
        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(s.getInputStream());
                // 每个客户端发送来的消息有多条，因此需要对每个客户端进行while true循环监听
                while (isStart) {
                    // 接收客户端
                    String strReceive = dis.readUTF();
                    serverTa.append(s.getInetAddress() + ":" + s.getPort() + ": " + strReceive + "\n");

                    // 回复客户端
                    String strSent = s.getInetAddress() + ":" + s.getPort() + ": " + strReceive + "\n";
                    // 需要将该消息广播到所有客户端
                    for (int i=0; i<clientConns.size(); i++) {
                        clientConns.get(i).send(strSent);
                    }
                }
            } catch (SocketException e1) {
                // 自行捕捉这条异常，将命令行红色报错信息自定义成自己想输出的内容
                serverTa.append("client " + s.getInetAddress() + ":" +
                        s.getPort() +  "has been offline\n");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        public void send(String str) {
            try {
                DataOutputStream dos = new DataOutputStream(this.s.getOutputStream());
                dos.writeUTF(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ServerChat serverChat = new ServerChat();
    }
}
