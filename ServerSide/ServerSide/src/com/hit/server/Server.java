package com.hit.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public final static int PORT = 2001;

    public static void main(String args[]) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port: " + PORT);
            while (true) {
                try {
                    Socket connection = server.accept();
                    System.out.println("New client just connected " + connection.getInetAddress().getHostAddress());
                    new Thread(new ClientThread(connection)).start();
                } catch (IOException e) {
                    //Do nothing, just one client failed to connect or something happened to it.
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: Could not start the server!");
        }
    }
}
