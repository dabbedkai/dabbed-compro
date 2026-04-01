package com.roshambo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.roshambo.RoshamboResult;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Type;

public class RoshamboServer {

    private static final String JSON_FILE = "players.json";
    private static ArrayList<Player> users = new ArrayList<>();
    
    // instantiate gson object
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // holding slot for the player waiting for a matchmaking game
    private static ClientHandler waitingPlayer = null;

    public static void main(String[] args) {
        int port = 6767;
        loadUsers();

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Roshambo Matchmaking Server Online! Listening on port " + port);

            while (true) {
                Socket client = server.accept();
                System.out.println("New player connected: " + client.getInetAddress().getHostAddress());
                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }


    public static synchronized void saveUsers() {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            gson.toJson(users, writer); // automatically writes the entire array as json to the file
        } catch (IOException e) {
            System.out.println("Error saving user state locally.");
        }
    }

    public static synchronized void loadUsers() {
        File f = new File(JSON_FILE);
        if (!f.exists()) return;

        try (FileReader reader = new FileReader(f)) {
            Type userListType = new TypeToken<ArrayList<Player>>(){}.getType();
            ArrayList<Player> loadedUsers = gson.fromJson(reader, userListType);
            
            if (loadedUsers != null) {
                users = loadedUsers;
            }
        } catch (Exception e) {
            System.out.println("Failed to read JSON Database.");
        }
    }
    
    // registeruser checks for duplicate usernames and adds new users to the list, then saves to JSON
    public static synchronized boolean registerUser(String username, String password) {
        for (Player u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false; // prevents duplicates
            }
        }
        users.add(new Player(username, password, 0));
        saveUsers();
        return true;
    }

    // authenticate checks if the provided credentials match any existing user and returns that user object if successful
    public static synchronized Player authenticate(String username, String password) {
        for (Player u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    // updatescore modifies a user's score and persists the change to the JSON file
    public static synchronized void updateScore(Player user, int scoreReward) {
        user.setScore(user.getScore() + scoreReward);
        saveUsers();
    }
    
    public static synchronized void printLeaderboard(ClientHandler p1, ClientHandler p2) {
        List<Player> sortedUsers = new ArrayList<>(users);
        sortedUsers.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        
        StringBuilder lb = new StringBuilder("\n=== GLOBAL LEADERBOARD ===\n");
        int rank = 1;
        for (Player p : sortedUsers) {
            lb.append(rank).append(". ").append(p.getUsername())
              .append(" - Overall Match Wins: ").append(p.getScore()).append("\n");
            rank++;
        }
        lb.append("==========================\n");
        
        String finalBoard = lb.toString();
        p1.out.println(finalBoard);
        p2.out.println(finalBoard);
    }

    // client handler class manages all interactions with a connected client, including login, registration, matchmaking, and gameplay
    private static class ClientHandler implements Runnable {
        private Socket socket;
        public BufferedReader in;
        public PrintWriter out;
        public Player loggedInUser = null;

        public ClientHandler(Socket s) {
            this.socket = s;
        }

        // main run method for the client handler thread
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Connection Established!");

                boolean running = true;
                while (running) {
                    out.println("\n--- ROSHAMBO PVP SERVER ---");
                    out.println("1. Login\n2. Create an Account\n3. Quit");
                    out.println("Option: ");
                    out.println("INPUT_REQUIRED");

                    String choice = in.readLine();
                    if (choice == null) break;

                    switch (choice) {
                        case "1":
                            if (executeLogin()) runMainMenu();
                            break;
                        case "2":
                            executeRegister();
                            break;
                        case "3":
                            out.println("QUIT_CLIENT");
                            running = false;
                            break;
                        default:
                            out.println("Invalid selection.");
                    }
                }
                socket.close();

            } catch (IOException e) {
                System.out.println("A player dropped connection.");
            } finally {
                synchronized (RoshamboServer.class) {
                    if (waitingPlayer == this) waitingPlayer = null;
                }
            }
        }

        // executelogin handles the login flow, prompting for credentials and authenticating against the user list
        private boolean executeLogin() throws IOException {
            out.println("Username: ");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();

            out.println("Password: ");
            out.println("INPUT_REQUIRED");
            String pw = in.readLine();

            Player authResult = authenticate(username, pw);
            if (authResult != null) {
                loggedInUser = authResult;
                out.println("Login Successful!");
                return true;
            }
            out.println("Incorrect login credentials.");
            return false;
        }

        // executetegister manages the account creation process, ensuring unique usernames and adding new users to the system
        private void executeRegister() throws IOException {
            out.println("Enter New Username: ");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();
            
            out.println("Enter New Password: ");
            out.println("INPUT_REQUIRED");
            String pass = in.readLine();
            
            if(registerUser(username, pass)) {
                out.println("Account created! Please log in.");
            } else {
                out.println("That username is already taken!");
            }
        }

        // runmainmenu displays the main menu for logged-in users, allowing them to find matches or log out
        private void runMainMenu() throws IOException {
            while (loggedInUser != null) {
                out.println("\n[ WELCOME " + loggedInUser.getUsername().toUpperCase() + " | WIN PTS: " + loggedInUser.getScore() + " ]");
                out.println("1. Find Match (PvP)\n2. Logout");
                out.println("Choice: ");
                out.println("INPUT_REQUIRED");

                String select = in.readLine();
                if(select == null) return;
                
                if (select.equals("1")) {
                    out.println("Entering matchmaking queue...");
                    findOpponent();
                } else if (select.equals("2")) {
                    out.println("Logging out.");
                    loggedInUser = null;
                } else {
                    out.println("Invalid choice.");
                }
            }
        }

        // findopponent implements a simple matchmaking system where the first player waits and the second player triggers the match start, then runs the game logic in a new thread
        private void findOpponent() {
            ClientHandler opponent = null;

            synchronized (RoshamboServer.class) {
                if (waitingPlayer == null) {
                    waitingPlayer = this;
                } else {
                    opponent = waitingPlayer;
                    waitingPlayer = null;
                }
            }

            if (opponent != null) {
                out.println("Match found! You are playing against " + opponent.loggedInUser.getUsername());
                opponent.out.println("Match found! You are playing against " + this.loggedInUser.getUsername());

                TwoPlayerGame match = new RoshamboMatch(opponent, this);
                match.run(); 

            } else {
                try {
                    out.println("Waiting for an opponent to join...");
                    synchronized (this) {
                        this.wait(); 
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // twoplayergame is an abstract class that defines the structure for any two-player game, including methods for broadcasting messages, handling player quits, validating moves, and evaluating winners. RoshamboMatch extends this class to implement the specific logic for Rock-Paper-Scissors.
    public static abstract class TwoPlayerGame implements Runnable {
        protected ClientHandler p1;
        protected ClientHandler p2;

        public TwoPlayerGame(ClientHandler p1, ClientHandler p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        protected void broadcast(String msg) {
            p1.out.println(msg);
            p2.out.println(msg);
        }

        protected void notifyPlayerQuit() {
            broadcast("Someone has surrendered/left.");
        }

        public abstract boolean isValidMove(String move);
        public abstract String evaluateWinner(String m1, String m2);
    }

    // roshambomatch implements the specific game logic for roshambo, including move validation, winner evaluation, and the main game loop that handles player interactions and score updates
    public static class RoshamboMatch extends TwoPlayerGame {
        
        public RoshamboMatch(ClientHandler p1, ClientHandler p2) {
            super(p1, p2); 
        }

        @Override
        public boolean isValidMove(String c) {
            return c.equals("0") || c.equals("1") || c.equals("2");
        }
        
        private String getActionName(String c) {
            switch(c) {
                case "0": return "Rock";
                case "1": return "Paper";
                case "2": return "Scissors";
                default: return "Unknown";
            }
        }

        @Override
        public String evaluateWinner(String m1, String m2) {
            if (m1.equals(m2)) return "DRAW";
            
            // map 0 = Rock, 1 = Paper, 2 = Scissors
            if ((m1.equals("0") && m2.equals("2")) ||
                (m1.equals("1") && m2.equals("0")) ||
                (m1.equals("2") && m2.equals("1"))) {
                return "P1";
            }
            return "P2";
        }
        
        // main game loop for the roshambo game, handling move input, validation, result evaluation, score updates, and showing results to both players
        @Override
        public void run() {
            try {
                int p1RoundWins = 0;
                int p2RoundWins = 0;

                broadcast("=== STARTING 10 ROUND MATCH ===");

                // game loop continues until a player quits or disconnects, handling move input and result evaluation each round
                for(int round = 1; round <= 10; round++) {
                    broadcast("\n-- ROUND " + round + " --");
                    p2.out.println("Waiting for Player 1 to make a move...");
                    p1.out.println("\nYOUR_TURN_RPS");

                    String move1 = p1.in.readLine();
                    if (move1 == null || move1.equalsIgnoreCase("quit")) {
                        notifyPlayerQuit(); break;
                    }

                    p1.out.println("Move locked. Waiting for Player 2...");
                    p2.out.println("\nYOUR_TURN_RPS"); 

                    String move2 = p2.in.readLine();
                    if (move2 == null || move2.equalsIgnoreCase("quit")) {
                        notifyPlayerQuit(); break;
                    }

                    move1 = move1.trim().toLowerCase();
                    move2 = move2.trim().toLowerCase();

                    if (!isValidMove(move1) || !isValidMove(move2)) {
                        broadcast("Invalid inputs detected. Round nullified and repeating round.");
                        round--; // Retry this round
                        continue;
                    }
                    
                    String act1 = getActionName(move1);
                    String act2 = getActionName(move2);

                    String resultString = evaluateWinner(move1, move2);
                    broadcast("--- ROUND RESULTS ---");
                    
                    if (resultString.equals("DRAW")) {
                        broadcast("It's a Tie! Both picked " + act1);
                    } else if (resultString.equals("P1")) {
                        p1RoundWins++;
                        p1.out.println("You won the round! " + act1 + " beats " + act2);
                        p2.out.println("You lost the round. " + act1 + " beats " + act2);
                    } else if (resultString.equals("P2")) {
                        p2RoundWins++;
                        p2.out.println("You won the round! " + act2 + " beats " + act1);
                        p1.out.println("You lost the round. " + act2 + " beats " + act1);
                    }

                    broadcast("CURRENT SCORE: " + p1.loggedInUser.getUsername() + "[" + p1RoundWins + "] vs " + 
                              p2.loggedInUser.getUsername() + "[" + p2RoundWins + "]");
                }

                // Lab Constraint Logic Location - Handle final evaluation outside of round looping
                String overallWinner;
                if (p1RoundWins > p2RoundWins) {
                    overallWinner = p1.loggedInUser.getUsername();
                    updateScore(p1.loggedInUser, 1); 
                } else if (p2RoundWins > p1RoundWins) {
                    overallWinner = p2.loggedInUser.getUsername();
                    updateScore(p2.loggedInUser, 1);
                } else {
                    overallWinner = "Tie - No overall winner points awarded.";
                }

                RoshamboResult matchFinalRecord = new RoshamboResult(
                    p1.loggedInUser.getUsername(),
                    p2.loggedInUser.getUsername(),
                    p1RoundWins, p2RoundWins,
                    overallWinner
                );

                broadcast("\n===== THE 10 ROUND MATCH IS OVER! =====");
                broadcast(matchFinalRecord.getFormattedSummary());
                
                // displays global json scoreboard  
                printLeaderboard(p1, p2); 

            } catch (IOException e) {
                System.out.println("Connection failed mid-game.");
            } finally {
                p1.out.println("Leaving Game Room. Sending you back to Menu.");
                p2.out.println("Leaving Game Room. Sending you back to Menu.");
                synchronized(p1) { p1.notify(); }
            }
        }
    }
}