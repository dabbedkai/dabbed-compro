package activity14;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8000;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner input = new Scanner(System.in)
        ) {
            
            Thread listener = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("\n" + serverMessage);
                        System.out.print("> "); 
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            });
            listener.start();

            System.out.print("> ");
            while (true) {
                String userInput = input.nextLine();
                out.println(userInput);

                if (userInput.equalsIgnoreCase("bye")) {
                    break;
                }
            }

            System.out.println("Closing connection...");

        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }
}