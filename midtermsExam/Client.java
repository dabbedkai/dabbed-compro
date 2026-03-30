package midtermsExam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client{
    public static void main(String[] args) {
        String serverAddress = "localhost"; 
        int port = 6767; 


        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connecting to the game Server...");
            boolean isMyTurn = false;
            String serverMessage;

            while ((serverMessage = in.readLine()) != null) {

                System.out.println("[SERVER] " + serverMessage);

                if (serverMessage.equalsIgnoreCase("Game ended. Disconnecting...")) {
                    break;
                }

                if (serverMessage.equalsIgnoreCase("Your turn")) {
                    isMyTurn = true;
                }

                if (isMyTurn) {
                    System.out.print("Enter your move (rock, paper, scissors) or type 'quit': ");
                    String myMove = sc.nextLine();  
                    
                    out.println(myMove); 
                    
                    isMyTurn = false; 
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: Server is offline or unavailable.");
        }
    }
}