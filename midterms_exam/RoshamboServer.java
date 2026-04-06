package com.roshambo.services;

import com.roshambo.models.*;
import com.google.gson.*; 
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Type;

public class RoshamboServer {

    private static final String JSON_FILE = "players.json";
    private static ArrayList<Player> users = new ArrayList<>();
    
    // instantiate gson object
    // implementing custom overrides forcing fields natively saving out as strings explicitly
    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            // routing specific object structures ignoring standard java inheritance parameters
            .registerTypeAdapter(Player.class, new JsonSerializer<Player>() {
                @Override
                public JsonElement serialize(Player src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject json = new JsonObject();
                    json.addProperty("username", src.getUsername());
                    json.addProperty("password", src.getPassword());
                    json.addProperty("matches played", src.getMatchesPlayed());
                    
                    double exactWinRate = Math.round(src.getWinRate() * 10.0) / 10.0;
                    json.addProperty("winrate", exactWinRate);
                    return json;
                }
            })
            // pushing strings back mathematically resolving states cleanly without failing out arrays
            .registerTypeAdapter(Player.class, new JsonDeserializer<Player>() {
                @Override
                public Player deserialize(JsonElement jsonElem, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject obj = jsonElem.getAsJsonObject();
                    
                    String un = obj.has("username") ? obj.get("username").getAsString() : "unknown";
                    String pw = obj.has("password") ? obj.get("password").getAsString() : "";
                    
                    int mp = 0;
                    if (obj.has("matches played")) mp = obj.get("matches played").getAsInt();
                    else if (obj.has("matchesPlayed")) mp = obj.get("matchesPlayed").getAsInt(); // gracefully checks deprecated formatting types
                    
                    int computedWins = 0;
                    if (obj.has("winrate")) {
                        double wr = obj.get("winrate").getAsDouble();
                        // recalculating missing match score based solely off percentages implicitly protecting system loads
                        computedWins = (int) Math.round((wr / 100.0) * mp); 
                    } else if (obj.has("score")) {
                        computedWins = obj.get("score").getAsInt();
                    }
                    
                    return new Player(un, pw, computedWins, mp);
                }
            })
            .create();
    
    // holding slot for the player waiting for a matchmaking game
    private static ClientHandler waitingPlayer = null;

    // ansi string layouts handling server aesthetics dynamically masking client requests properly 
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";

    public static void main(String[] args) {
        int port = 6767;
        loadUsers();

        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println(CYAN + "Roshambo Matchmaking Server Online! Listening on port " + port + RESET);

            while (true) {
                Socket client = server.accept();
                System.out.println(GREEN + "New player connected: " + client.getInetAddress().getHostAddress() + RESET);
                
                ClientHandler ch = new ClientHandler(client);
                Thread thread = new Thread(ch);
                thread.start();
            }
        } catch (Exception e) {
            System.out.println(RED + "Server exception: " + RESET);
            e.printStackTrace();
        }
    }


    public static synchronized void saveUsers() {
        try {
            FileWriter writer = new FileWriter(JSON_FILE);
            // automatically writes the entire array as json to the file
            gson.toJson(users, writer); 
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving user state locally.");
        }
    }

    public static synchronized void loadUsers() {
        File f = new File(JSON_FILE);
        if (!f.exists()) {
            return;
        }

        try {
            FileReader reader = new FileReader(f);
            Type userListType = new TypeToken<ArrayList<Player>>(){}.getType();
            ArrayList<Player> loadedUsers = gson.fromJson(reader, userListType);
            
            if (loadedUsers != null) {
                users = loadedUsers;
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Failed to read JSON Database.");
        }
    }
    
    // registeruser checks for duplicate usernames and adds new users to the list, then saves to JSON
    public static synchronized boolean registerUser(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            Player u = users.get(i);
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false; // prevents duplicates
            }
        }
        
        Player newP = new Player(username, password, 0, 0);
        users.add(newP);
        saveUsers();
        
        return true;
    }

    // authenticate checks if the provided credentials match any existing user and returns that user object if successful
    public static synchronized Player authenticate(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            Player u = users.get(i);
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    // updatescore modifies a user's score and persists the change to the JSON file
    public static synchronized void updatePlayerStats(Player user, boolean isWinner) {
        // increase total matches played for this user no matter what
        user.setMatchesPlayed(user.getMatchesPlayed() + 1);
        
        // increase their score (win tally) only if they actually won
        if (isWinner) {
            user.incrementScore(); // securely updating through model rules 
        }
        
        saveUsers();
    }
    
    public static synchronized void printLeaderboard(ClientHandler p1, ClientHandler p2) {
        List<Player> sortedUsers = new ArrayList<>(users);
        
        // loop sorting to order by win rate percentage
        for (int i = 0; i < sortedUsers.size(); i++) {
            for (int j = i + 1; j < sortedUsers.size(); j++) {
                
                double winRateA = sortedUsers.get(i).getWinRate();
                double winRateB = sortedUsers.get(j).getWinRate();
                
                // sort highest to lowest win rate
                if (winRateA < winRateB) {
                    Collections.swap(sortedUsers, i, j);
                } 
                // if there is a tie, let the player with more actual games played take higher rank
                else if (winRateA == winRateB) {
                    if (sortedUsers.get(i).getMatchesPlayed() < sortedUsers.get(j).getMatchesPlayed()) {
                        Collections.swap(sortedUsers, i, j);
                    }
                }
            }
        }
        
        StringBuilder lb = new StringBuilder();
        lb.append("\n").append(MAGENTA).append(BOLD).append("GLOBAL LEADERBOARD (WIN RATE)\n").append(RESET);
        lb.append(MAGENTA).append("─────────────────────────────────────\n").append(RESET);
        
        int rank = 1;
        for (int i = 0; i < sortedUsers.size(); i++) {
            Player p = sortedUsers.get(i);
            
            // round the win rate to 1 decimal place for display purposes
            double roundedWr = Math.round(p.getWinRate() * 10.0) / 10.0;
            
            String color = (rank == 1) ? YELLOW : (rank == 2) ? CYAN : RESET;
            lb.append(color).append(rank).append(". ").append(p.getUsername())
              .append(" - ").append(roundedWr).append("% (")
              .append(p.getScore()).append("W / ").append(p.getMatchesPlayed()).append(" Total)\n").append(RESET);
            rank++;
        }
        lb.append(MAGENTA).append("─────────────────────────────────────\n").append(RESET);
        
        p1.out.println(lb.toString());
        p2.out.println(lb.toString());
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

                out.println(GREEN + "Connection Established!" + RESET);

                boolean running = true;
                while (running) {
                    out.println("\n" + CYAN + "╔══════════════════════════════╗");
                    out.println("║    " + BOLD + "ROSHAMBO PvP SERVER" + RESET + CYAN + "       ║");
                    out.println("╚══════════════════════════════╝" + RESET);
                    out.println(" 1. Login");
                    out.println(" 2. Create an Account");
                    out.println(" 3. Quit");
                    out.println("\nOption:");
                    out.println("INPUT_REQUIRED");

                    String choice = in.readLine();
                    if (choice == null) {
                        break;
                    }

                    switch (choice) {
                        case "1":
                            boolean logged = executeLogin();
                            if (logged) {
                                runMainMenu();
                            }
                            break;
                        case "2":
                            executeRegister();
                            break;
                        case "3":
                            out.println("QUIT_CLIENT");
                            running = false;
                            break;
                        default:
                            out.println(RED + "Invalid selection." + RESET);
                    }
                }
                socket.close();

            } catch (IOException e) {
                System.out.println(RED + "A player dropped connection." + RESET);
            } finally {
                synchronized (RoshamboServer.class) {
                    if (waitingPlayer == this) {
                        waitingPlayer = null;
                    }
                }
            }
        }

        // executelogin handles the login flow, prompting for credentials and authenticating against the user list
        private boolean executeLogin() throws IOException {
            out.println("\nUsername:");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();

            out.println("Password:");
            out.println("INPUT_REQUIRED");
            String pw = in.readLine();
            
            // basic input validation blocking blank spaces preventing server array corruption
            if (username == null || username.trim().isEmpty() || pw == null || pw.trim().isEmpty()) {
                out.println(RED + "Error: username or password cannot be empty." + RESET);
                return false;
            }

            Player authResult = authenticate(username, pw);
            if (authResult != null) {
                loggedInUser = authResult;
                out.println(GREEN + "Login Successful!" + RESET);
                return true;
            }
            
            out.println(RED + "Incorrect login credentials." + RESET);
            return false;
        }

        // executetegister manages the account creation process, ensuring unique usernames and adding new users to the system
        private void executeRegister() throws IOException {
            out.println("\nEnter New Username:");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();
            
            out.println("Enter New Password:");
            out.println("INPUT_REQUIRED");
            String pass = in.readLine();
            
            // standard school friendly string checking verifying fields aren't completely blank spaces 
            if (username == null || username.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
                out.println(RED + "Registration failed! Please use real characters." + RESET);
                return;
            }
            
            if(registerUser(username, pass)) {
                out.println(GREEN + "Account created! Please log in." + RESET);
            } else {
                out.println(RED + "That username is already taken!" + RESET);
            }
        }

        // runmainmenu displays the main menu for logged-in users, allowing them to find matches or log out
        private void runMainMenu() throws IOException {
            while (loggedInUser != null) {
                double dispRateRaw = loggedInUser.getWinRate();
                double dispRateRound = Math.round(dispRateRaw * 10.0) / 10.0;
                
                out.println("\n" + BLUE + "╭──────────────────────────────────────────╮");
                out.println("│  " + BOLD + "PLAYER:" + RESET + " " + String.format("%-31s", loggedInUser.getUsername().toUpperCase()) + BLUE + "│");
                out.println("│  " + BOLD + "WIN RATE:" + RESET + " " + String.format("%-29s", dispRateRound + "%") + BLUE + "│");
                out.println("╰──────────────────────────────────────────╯" + RESET);
                out.println(" 1. Find Match (PvP)");
                out.println(" 2. Logout");
                out.println("\nChoice:");
                out.println("INPUT_REQUIRED");

                String select = in.readLine();
                if(select == null) return;
                
                if (select.equals("1")) {
                    out.println(YELLOW + "Entering matchmaking queue..." + RESET);
                    findOpponent();
                } else if (select.equals("2")) {
                    out.println("Logging out.");
                    loggedInUser = null;
                } else {
                    out.println(RED + "Invalid choice." + RESET);
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
                out.println(GREEN + BOLD + "Match found! You are playing against " + opponent.loggedInUser.getUsername() + RESET);
                opponent.out.println(GREEN + BOLD + "Match found! You are playing against " + this.loggedInUser.getUsername() + RESET);

                // fires off strictly mapped state encapsulation instead of legacy math calls 
                GameSession match = new GameSession(opponent, this);
                match.run(); 

            } else {
                try {
                    out.println(YELLOW + "Waiting for an opponent to join..." + RESET);
                    synchronized (this) {
                        this.wait(); 
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // handles logic locking completely stopping data pollution out from core threading logic 
    private static class GameSession implements Runnable {
        private ClientHandler p1;
        private ClientHandler p2;

        public GameSession(ClientHandler p1, ClientHandler p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        private void broadcast(String msg) {
            p1.out.println(msg);
            p2.out.println(msg);
        }

        private void notifyPlayerQuit() {
            broadcast(RED + "[!] Someone has surrendered/left the game." + RESET);
        }

        // securely checks and pushes plain texts converting logic models polymorphism requirements gracefully 
        private GameMove parseMove(String input) {
            if (input.equals("0")) { return new Rock(); }
            if (input.equals("1")) { return new Paper(); }
            if (input.equals("2")) { return new Scissors(); }
            return null; // implicitly manages strict format validation dropping out errors nicely 
        }
        
        // main game loop for the roshambo game, handling move input, validation, result evaluation, score updates, and showing results to both players
        @Override
        public void run() {
            try {
                int p1Wins = 0;
                int p2Wins = 0;

                broadcast("\n" + YELLOW + BOLD + "╔════════════════════════════════╗");
                broadcast("║  === MATCH IS STARTING! ===    ║");
                broadcast("║      BEST OF 10 ROUNDS         ║");
                broadcast("╚════════════════════════════════╝" + RESET);

                // game loop continues until a player quits or disconnects, handling move input and result evaluation each round
                for(int round = 1; round <= 10; round++) {
                    broadcast("\n" + CYAN + "╭──────── ROUND " + round + " ────────╮" + RESET);
                    
                    // natively unblocking prompts launching asynchronous string retrievals evenly 
                    broadcast("YOUR_TURN_RPS");

                    // mapping string responses wrapping individual background thread containers implicitly 
                    String[] moves = new String[2];

                    Thread t1 = new Thread(() -> {
                        try {
                            moves[0] = p1.in.readLine();
                            // securely masking client screen immediately acknowledging responses properly 
                            if (moves[0] != null && !moves[0].equalsIgnoreCase("quit")) {
                                p1.out.println(YELLOW + "Move locked. Waiting for opponent..." + RESET);
                            }
                        } catch (IOException e) { moves[0] = null; }
                    });

                    Thread t2 = new Thread(() -> {
                        try {
                            moves[1] = p2.in.readLine();
                            // securely masking client screen immediately acknowledging responses properly 
                            if (moves[1] != null && !moves[1].equalsIgnoreCase("quit")) {
                                p2.out.println(YELLOW + "Move locked. Waiting for opponent..." + RESET);
                            }
                        } catch (IOException e) { moves[1] = null; }
                    });

                    t1.start();
                    t2.start();
                    
                    // barricades game process explicitly waiting out string completions universally
                    t1.join();
                    t2.join();

                    String move1 = moves[0];
                    String move2 = moves[1];

                    if (move1 == null || move1.equalsIgnoreCase("quit") || move2 == null || move2.equalsIgnoreCase("quit")) {
                        notifyPlayerQuit(); 
                        break;
                    }

                    move1 = move1.trim().toLowerCase();
                    move2 = move2.trim().toLowerCase();
                    
                    GameMove m1obj = parseMove(move1);
                    GameMove m2obj = parseMove(move2);

                    if (m1obj == null || m2obj == null) {
                        broadcast(RED + "Invalid inputs detected. Round nullified and repeating." + RESET);
                        // retry this round gracefully trapping incorrect terminal presses
                        round--; 
                        continue;
                    }
                    
                    // assign dynamically parsed choices deeply internally checking parameters
                    p1.loggedInUser.setCurrentMove(m1obj);
                    p2.loggedInUser.setCurrentMove(m2obj);

                    // abstracts out mathematics utilizing polymorphic checking naturally passing states 
                    int compRes = p1.loggedInUser.getCurrentMove().compare(p2.loggedInUser.getCurrentMove());
                    
                    String act1 = p1.loggedInUser.getCurrentMove().getMoveName();
                    String act2 = p2.loggedInUser.getCurrentMove().getMoveName();

                    broadcast("\n" + BLUE + "     --- RESULTS ---     " + RESET);
                    
                    if (compRes == 0) {
                        broadcast(YELLOW + "It's a Tie! Both picked " + act1 + RESET);
                    } else if (compRes == 1) {
                        p1Wins++;
                        p1.out.println(GREEN + "You won the round! " + act1 + " beats " + act2 + RESET);
                        p2.out.println(RED + "You lost the round. " + act1 + " beats " + act2 + RESET);
                    } else if (compRes == -1) {
                        p2Wins++;
                        p2.out.println(GREEN + "You won the round! " + act2 + " beats " + act1 + RESET);
                        p1.out.println(RED + "You lost the round. " + act2 + " beats " + act1 + RESET);
                    }

                    broadcast(CYAN + "SCORE: " + p1.loggedInUser.getUsername() + "[" + p1Wins + "] vs " + 
                              p2.loggedInUser.getUsername() + "[" + p2Wins + "]" + RESET);
                }

                String overallWinner;
                
                if (p1Wins > p2Wins) {
                    overallWinner = p1.loggedInUser.getUsername();
                    updatePlayerStats(p1.loggedInUser, true); 
                    updatePlayerStats(p2.loggedInUser, false);
                } else if (p2Wins > p1Wins) {
                    overallWinner = p2.loggedInUser.getUsername();
                    updatePlayerStats(p2.loggedInUser, true);
                    updatePlayerStats(p1.loggedInUser, false);
                } else {
                    overallWinner = "Tie - No overall winner points awarded.";
                    // it is a tie, so they both get +1 match played but neither gets +1 win tally
                    updatePlayerStats(p1.loggedInUser, false);
                    updatePlayerStats(p2.loggedInUser, false);
                }

                broadcast("\n" + YELLOW + BOLD + "╔═════════════════════════════════════════╗");
                broadcast("║       THE 10 ROUND MATCH IS OVER!       ║");
                broadcast("╚═════════════════════════════════════════╝" + RESET);
                
                String p1Color = (p1Wins > p2Wins) ? GREEN : (p1Wins < p2Wins) ? RED : YELLOW;
                String p2Color = (p2Wins > p1Wins) ? GREEN : (p2Wins < p1Wins) ? RED : YELLOW;
                
                broadcast(p1Color + p1.loggedInUser.getUsername() + "[" + p1Wins + " pts]" + RESET + BOLD + " VS " + RESET +
                          p2Color + p2.loggedInUser.getUsername() + " [" + p2Wins + " pts]" + RESET);
                broadcast(YELLOW + "WINNER: " + overallWinner + RESET);
                
                // displays global json scoreboard  
                printLeaderboard(p1, p2); 

            } catch (Exception e) {
                System.out.println(RED + "Connection failed mid-game." + RESET);
            } finally {
                p1.out.println(BLUE + "Leaving Game Room. Sending you back to Menu." + RESET);
                p2.out.println(BLUE + "Leaving Game Room. Sending you back to Menu." + RESET);
                
                synchronized(p1) { 
                    p1.notify(); 
                }
            }
        }
    }
}