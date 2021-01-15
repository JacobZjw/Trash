package com.WeChat.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author Jv____
 */
public class Server {
    final private int port = 9999;

    public Server() {
        try {
            DataBuffer.serverSocket = new ServerSocket(port);
            ExecutorService pool = Executors.newCachedThreadPool();
//            ExecutorService pool = new ThreadPoolExecutor();

            while (true) {
                Socket socket = DataBuffer.serverSocket.accept();
                Callable client = new ListenRequest(socket);
                pool.submit(client);
                System.out.println("收到来自"
                        + socket.getInetAddress().getHostAddress()
                        + ":" + socket.getPort() + "的新连接 !");
            }
        } catch (IOException e) {
            System.out.println("Connect fail!");
        } finally {
        }
    }

    public static void main(String[] args) throws IOException {
        // write your code here
        new Server();
    }
}
