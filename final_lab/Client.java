package com.manabrew.network;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 8080);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            // thread to read server messages without blocking typing
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("disconnected from tavern.");
                }
            }).start();

            // main thread to send commands
            while (true) {
                String cmd = scanner.nextLine();
                out.println(cmd);
                if (cmd.equals("quit")) break;
            }
        } catch (Exception e) {
            System.out.println("could not connect to the tavern server.");
        }
    }
}