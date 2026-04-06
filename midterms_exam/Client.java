package com.roshambo;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    // ansi colors for the client-side prompts visually routing texts and inputs in the terminal
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 6767;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println(CYAN + BOLD + "Connecting to the Roshambo Server..." + RESET);
            String serverMessage;

            while ((serverMessage = in.readLine()) != null) {
                // if the server forces client closure logic securely terminates process looping properly 
                if (serverMessage.equalsIgnoreCase("QUIT_CLIENT") || serverMessage.equalsIgnoreCase("Game ended. Disconnecting...")) {
                    System.out.println("Disconnected from server. Goodbye!");
                    break;
                }

                // handling basic menu prompts mapping terminal blocks
                if (serverMessage.equalsIgnoreCase("INPUT_REQUIRED")) {
                    System.out.print(CYAN + BOLD + " > " + RESET);
                    out.println(sc.nextLine());
                } 
                // handling specific in-game moves
                else if (serverMessage.equalsIgnoreCase("YOUR_TURN_RPS")) {
                    System.out.print(YELLOW + BOLD + "[?] Your move (0=Rock, 1=Paper, 2=Scissors, quit): " + RESET);
                    out.println(sc.nextLine());
                } 
                else {
                    System.out.println(serverMessage);
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: Server is offline or unavailable.");
        }
    }
}