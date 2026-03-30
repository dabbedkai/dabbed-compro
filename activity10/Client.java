package activity10;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 6767;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Successfully connected to the Hangman Server at " + serverAddress + ":" + port);

            String serverMessage;

            while ((serverMessage = in.readLine()) != null) {
                
                if (serverMessage.equals("INPUT_REQUIRED")) {
                    String userInput = scanner.nextLine();
                    out.println(userInput);
                    
                } else if (serverMessage.equals("QUIT")) {
                    break;
                    
                } else {

                    System.out.println(serverMessage);
                }
            }

        } catch (IOException e) {
            System.out.println("Client Error: Could not connect to the server. Make sure it is running.");
            System.out.println("Details: " + e.getMessage());
        }
    }
}