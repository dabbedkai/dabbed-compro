package activity10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class RoshamboServer {
    public static void main(String[] args) {
        int port = 6767;

        try (ServerSocket server = new ServerSocket(port);
                Scanner sc = new Scanner(System.in);) {

            System.out.println("Waiting for Player 1 to connect...");
            Socket player1 = server.accept();
            System.out.println("Player 1 connected!");
            PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
            out1.println("Welcome Player 1! Waiting for Player 2 to join...");

            // Wait for Player 2
            System.out.println("Waiting for Player 2 to connect...");
            Socket player2 = server.accept();
            System.out.println("Player 2 connected!");
            PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));

            out1.println("Player 2 has joined. Game starting!");
            out2.println("Welcome Player 2! Both players connected. Game starting!");

            while (true) {

                out1.println("Your turn");
                out2.println("Wait for your turn");

                String move1 = in1.readLine();
                if (move1 == null || move1.equalsIgnoreCase("quit")) {
                    System.out.println("A player has quit. Shutting down server.");
                    out1.println("Game ended. Disconnecting...");
                    out2.println("Game ended. Disconnecting...");
                    break;
                }

                out2.println("Your turn");
                out1.println("Wait for other player's pick");

                String move2 = in2.readLine();
                if (move2 == null || move2.equalsIgnoreCase("quit")) {
                    System.out.println("A player has quit. Shutting down server.");
                    out1.println("Game ended. Disconnecting...");
                    out2.println("Game ended. Disconnecting...");
                    break;
                }

                move1 = move1.toLowerCase();
                move2 = move2.toLowerCase();

                String result = "";
                if (!isValid(move1.charAt(0)) || !isValid(move2.charAt(0))) {
                    result = "Invalid input detected. Round canceled.";

                } else {

                    if (move1.equals(move2)) {
                        out1.println("It's a Draw!");
                        out2.println("It's a Draw!");
                    } else if ((move1.equals("rock") && move2.equals("scissors")) ||
                            (move1.equals("paper") && move2.equals("rock")) ||
                            (move1.equals("scissors") && move2.equals("paper"))) {
                        out1.println("You Win!");
                        out2.println("You Lose!");
                    } else {
                        out1.println("You Lose!");
                        out2.println("You Win!");
                    }
                }

                out1.println("------------------------------------------");
                out1.println("You played: " + move1);
                out1.println("Opponent played: " + move2);
                out1.println("Result: " + result);
                out1.println("------------------------------------------");

                out2.println("------------------------------------------");
                out2.println("You played: " + move2);
                out2.println("Opponent played: " + move1);
                out2.println("Result: " + result);
                out2.println("------------------------------------------");
            }

            player1.close();
            player2.close();

        } catch (IOException e) {
            System.out.println("Server Exception: " + e.getMessage());
        }
    }

    private static boolean isValid(char c) {
        return c == 'r' || c == 'p' || c == 's';
    }
}
