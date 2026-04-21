package activity14;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 8000;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is up and running on port " + PORT + "!");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A new client connected from " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }

    static synchronized void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    static synchronized void addWriter(PrintWriter writer) {
        clientWriters.add(writer);
    }

    static synchronized void removeWriter(PrintWriter writer) {
        clientWriters.remove(writer);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Welcome to the server! Please enter your name:");
            clientName = in.readLine();

            Server.addWriter(out);
            Server.broadcast(clientName + " has joined the room!");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("bye")) {
                    break;
                }
                Server.broadcast(clientName + ": " + message);
            }

        } catch (IOException e) {
            System.out.println("Lost connection to " + clientName);
        } finally {
            if (clientName != null && out != null) {
                Server.removeWriter(out);
                Server.broadcast(clientName + " has left the room.");
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not properly close connection.");
            }
        }
    }
}
