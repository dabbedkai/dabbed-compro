package activity10;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class HangmanServer {

    private static final String JSON_FILE = "users.json";
    private static List<User> users = new ArrayList<>();

    public static void main(String[] args) {

        int port = 6767;
        loadUsers();

        try (ServerSocket server = new ServerSocket(port)) {

            System.out.println("Hangman Server started! Waiting for clients...");

            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected: " + client.getInetAddress());

                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            System.out.println("Server Exception: " + e.getMessage());
        }
    }
    public static synchronized void saveUsers() {

        try {

            StringBuilder sb = new StringBuilder("[\n");

            for (int i = 0; i < users.size(); i++) {

                sb.append("  ").append(users.get(i).toJson());

                if (i < users.size() - 1) sb.append(",");
                sb.append("\n");
            }

            sb.append("]");
            Files.write(Paths.get(JSON_FILE), sb.toString().getBytes());

        } catch (IOException e) {

            System.out.println("Error saving user data.");

        }
    }

    public static synchronized void loadUsers() {

        users.clear();

        try {

            Path path = Paths.get(JSON_FILE);

            if (!Files.exists(path)) {

                Files.write(path, "[]".getBytes());

            }
            String content = new String(Files.readAllBytes(path));
            
            Pattern p = Pattern.compile("\\{\"username\":\"(.*?)\", \"password\":\"(.*?)\", \"score\":(\\d+)\\}");
            Matcher m = p.matcher(content);
            
            while (m.find()) {

                users.add(new User(m.group(1), m.group(2), Integer.parseInt(m.group(3))));

            }
        } catch (IOException e) {

            System.out.println("Error loading user data.");

        }
    }

    public static synchronized boolean registerUser(String username, String password) {

        for (User u : users) {

            if (u.getUsername().equals(username)) return false;

        }

        users.add(new User(username, password, 0));
        saveUsers();
        return true;
    }

    public static synchronized User authenticate(String username, String password) {

        for (User u : users) {
            
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {

                return u;

            }
        }

        return null;
    }

    public static synchronized void updateScore(User user, int scoreReward) {

        user.setScore(user.getScore() + scoreReward);
        saveUsers();

    }

    public static List<String> readWordsFromFile(String filename) {

        try {

            return Files.readAllLines(Paths.get(filename));

        } catch (IOException e) {

            return Arrays.asList("apple", "banana", "computer", "java", "network");

        }
    }
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private User loggedInUser = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("Welcome Player!");

                while (true) {

                    out.println("\n=== HANGMAN LOGIN SYSTEM ===");
                    out.println("1. Login");
                    out.println("2. Register new account");
                    out.println("3. Exit");
                    out.println("Choose an option: ");
                    out.println("INPUT_REQUIRED");
                    
                    String choice = in.readLine();
                    if (choice == null) break;

                    if (choice.equals("1")) {

                        if (login(in, out)) {
                            mainMenu(in, out);
                        }

                    } else if (choice.equals("2")) {

                        register(in, out);

                    } else if (choice.equals("3")) {

                        out.println("Goodbye!");
                        out.println("QUIT");
                        break;

                    } else {

                        out.println("Invalid choice. Try again.");

                    }
                }

            } catch (IOException e) {

                System.out.println("A client disconnected.");

            }
        }

        private boolean login(BufferedReader in, PrintWriter out) throws IOException {

            out.println("Enter username: ");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();

            out.println("Enter password: ");
            out.println("INPUT_REQUIRED");
            String password = in.readLine();

            User user = authenticate(username, password);
            if (user != null) {
                
                this.loggedInUser = user;
                out.println("Login successful!");
                return true;

            } else {

                out.println("Invalid username or password.");
                return false;

            }
        }

        private void register(BufferedReader in, PrintWriter out) throws IOException {

            out.println("Enter new username: ");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();

            out.println("Enter new password: ");
            out.println("INPUT_REQUIRED");
            String password = in.readLine();

            if (registerUser(username, password)) {

                out.println("Registration successful! You can now login.");

            } else {

                out.println("Username already exists. Try another.");

            }
        }

        private void mainMenu(BufferedReader in, PrintWriter out) throws IOException {

            while (loggedInUser != null) {
                out.println("\n=== MAIN MENU ===");
                out.println("Player: " + loggedInUser.getUsername() + " | Score: " + loggedInUser.getScore());
                out.println("1. Play Hangman");
                out.println("2. Logout");
                out.println("Choose an option: ");
                out.println("INPUT_REQUIRED");

                String choice = in.readLine();
                if (choice == null) return;

                if (choice.equals("1")) {

                    playGame(in, out);

                } else if (choice.equals("2")) {

                    loggedInUser = null;
                    out.println("Logged out successfully.");

                } else {

                    out.println("Invalid choice.");

                }
            }
        }

        private void playGame(BufferedReader in, PrintWriter out) throws IOException {
            
            out.println("\nSelect Difficulty:\n1. Easy\n2. Medium\n3. Hard\nChoice: ");
            out.println("INPUT_REQUIRED");
            String diff = in.readLine();
            if (diff == null) return;

            String filename = "easy.txt";
            int scoreReward = 10;

            if (diff.equals("2")) {

                filename = "medium.txt";
                scoreReward = 20;

            } else if (diff.equals("3")) {

                filename = "hard.txt";
                scoreReward = 30;

            }

            List<String> words = readWordsFromFile(filename);

            if (words.isEmpty()) {

                out.println("Error: No words found.");
                return;

            }

            String wordToGuess = words.get((int) (Math.random() * words.size())).toLowerCase();
            int maxAttempts = 6;
            int correctGuesses = 0;
            char[] blankSpace = new char[wordToGuess.length()];
            Arrays.fill(blankSpace, '_');

            out.println("\nGame started! Difficulty: " + filename.replace(".txt", "").toUpperCase());

            while (true) {

                out.println("\nAttempts left: " + maxAttempts);
                StringBuilder sb = new StringBuilder();
                for (char c : blankSpace) sb.append(c).append(" ");
                
                out.println(sb.toString());
                out.println("Choose a letter to guess the word: ");
                out.println("INPUT_REQUIRED");

                String input = in.readLine();
                if (input == null) return;
                if (input.isEmpty()) continue;
                
                char guess = input.toLowerCase().charAt(0);
                boolean found = false;
                boolean alreadyGuessed = true;

                for (int i = 0; i < wordToGuess.length(); i++) {

                    if (wordToGuess.charAt(i) == guess) {

                        found = true;
                        if (blankSpace[i] == '_') {
                            blankSpace[i] = guess;
                            correctGuesses++;
                            alreadyGuessed = false;

                        }
                    }
                }

                if (found) {

                    if (alreadyGuessed) out.println("You already guessed '" + guess + "'.");
                    else out.println(guess + " is a great guess!");

                } else {

                    out.println(guess + " is not in the word.");
                    maxAttempts--;

                }

                if (correctGuesses == wordToGuess.length()) {

                    out.println("\nCongratulations! You've guessed the word: " + wordToGuess);
                    out.println("You earned " + scoreReward + " points!");
                    updateScore(loggedInUser, scoreReward);
                    break;
                    
                }

                if (maxAttempts == 0) {

                    out.println("\nGame Over! The word was: " + wordToGuess);
                    break;

                }
            }
        }
    }
}