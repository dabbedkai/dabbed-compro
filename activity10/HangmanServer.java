package com.hangman;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.Type;

public class HangmanServer {

    private static final String JSON_FILE = "users.json";
    private static ArrayList<User> users = new ArrayList<>();

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        int port = 6767;
        loadUsers();

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Hangman Server started! Waiting for clients...");

            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

                new Thread(new ClientHandler(client)).start();
            }
        } catch (IOException e) {
            System.out.println("Server Exception: " + e.getMessage());
        }
    }

    public static synchronized void saveUsers() {
        try (FileWriter writer = new FileWriter(JSON_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.out.println("Error saving user state locally.");
        }
    }

    public static synchronized void loadUsers() {
        File file = new File(JSON_FILE);
        if (!file.exists())
            return;

        try (FileReader reader = new FileReader(file)) {
            Type userListType = new TypeToken<ArrayList<User>>() {
            }.getType();
            ArrayList<User> loadedUsers = gson.fromJson(reader, userListType);

            if (loadedUsers != null) {
                users = loadedUsers;
            }
        } catch (Exception e) {
            System.out.println("Could not parse JSON User Database.");
        }
    }

    public static synchronized boolean registerUser(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        users.add(new User(username, password, 0));
        saveUsers();
        return true;
    }

    public static synchronized User authenticate(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public static synchronized void updateScore(User user, int scoreReward) {
        user.setScore(user.getScore() + scoreReward);
        saveUsers();
    }

    // --- Simple Thread for handling the Client Menus and Games directly ---

    private static class ClientHandler implements Runnable {
        private Socket socket;
        public BufferedReader in;
        public PrintWriter out;
        public User loggedInUser = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Welcome Player!");

                boolean running = true;
                while (running) {
                    out.println(
                            "\n=== HANGMAN LOGIN SYSTEM ===\n1. Login\n2. Register new account\n3. Exit\nChoose an option:");
                    out.println("INPUT_REQUIRED");

                    String choice = in.readLine();
                    if (choice == null)
                        break;

                    switch (choice.trim()) {
                        case "1":
                            if (login())
                                mainMenu();
                            break;
                        case "2":
                            register();
                            break;
                        case "3":
                            out.println("Goodbye!");
                            out.println("QUIT");
                            running = false;
                            break;
                        default:
                            out.println("Invalid choice. Try again.");
                    }
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("A client suddenly disconnected.");
            }
        }

        private boolean login() throws IOException {
            out.println("Enter username:");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();

            out.println("Enter password:");
            out.println("INPUT_REQUIRED");
            String password = in.readLine();

            User user = authenticate(username, password);
            if (user != null) {
                this.loggedInUser = user;
                out.println("Login successful!");
                return true;
            }
            out.println("Invalid username or password.");
            return false;
        }

        private void register() throws IOException {
            out.println("Enter new username:");
            out.println("INPUT_REQUIRED");
            String username = in.readLine();

            out.println("Enter new password:");
            out.println("INPUT_REQUIRED");
            String password = in.readLine();

            if (registerUser(username, password)) {
                out.println("Registration successful! You can now login.");
            } else {
                out.println("Username already exists. Try another.");
            }
        }

        private void mainMenu() throws IOException {
            while (loggedInUser != null) {
                out.println("\n=== MAIN MENU ===");
                out.println("Player: " + loggedInUser.getUsername() + " | Score: " + loggedInUser.getScore());
                out.println("1. Play Hangman\n2. Logout\nChoose an option:");
                out.println("INPUT_REQUIRED");

                String choice = in.readLine();
                if (choice == null)
                    return;

                if (choice.equals("1")) {
                    playGame(); // Calls direct function
                } else if (choice.equals("2")) {
                    loggedInUser = null;
                    out.println("Logged out successfully.");
                } else {
                    out.println("Invalid choice.");
                }
            }
        }

        private void playGame() throws IOException {
            out.println("\nSelect Difficulty:\n1. Easy\n2. Medium\n3. Hard\nChoice: ");
            out.println("INPUT_REQUIRED");

            String diff = in.readLine();
            if (diff == null)
                return;

            String filename = "easy.txt";
            int scoreReward = 10;

            if (diff.equals("2")) {
                filename = "medium.txt";
                scoreReward = 20;
            } else if (diff.equals("3")) {
                filename = "hard.txt";
                scoreReward = 30;
            }

            List<String> words = readWords(filename);
            if (words.isEmpty()) {
                out.println("Error: No words loaded in dictionary.");
                return;
            }

            Random random = new Random();
            String wordToGuess = words.get(random.nextInt(words.size())).toLowerCase();

            int maxAttempts = 6;
            int correctGuesses = 0;

            char[] blankSpace = new char[wordToGuess.length()];
            Arrays.fill(blankSpace, '_');
            List<Character> guessedLetters = new ArrayList<>();

            out.println("\nGame started!");

            while (maxAttempts > 0 && correctGuesses < wordToGuess.length()) {
                out.println("\nAttempts left: " + maxAttempts);

                StringBuilder wordDisplay = new StringBuilder();
                for (char c : blankSpace)
                    wordDisplay.append(c).append(" ");
                out.println(wordDisplay.toString());

                out.println("Choose a letter to guess: ");
                out.println("INPUT_REQUIRED");

                String input = in.readLine();
                if (input == null)
                    return;

                input = input.trim().toLowerCase();
                if (input.isEmpty()) {
                    out.println("Please enter a valid letter.");
                    continue;
                }
                char guess = input.charAt(0);

                if (guessedLetters.contains(guess)) {
                    out.println("You already guessed '" + guess + "'. Try a different one.");
                    continue;
                }

                guessedLetters.add(guess);
                boolean foundMatch = false;

                for (int i = 0; i < wordToGuess.length(); i++) {
                    if (wordToGuess.charAt(i) == guess) {
                        blankSpace[i] = guess;
                        correctGuesses++;
                        foundMatch = true;
                    }
                }

                if (foundMatch)
                    out.println(guess + " is a correct guess!");
                else {
                    out.println(guess + " is not in the word.");
                    maxAttempts--;
                }
            }

            if (correctGuesses == wordToGuess.length()) {
                out.println("\nCongratulations! You've guessed the word: " + wordToGuess);
                out.println("You earned " + scoreReward + " points!");
                updateScore(loggedInUser, scoreReward);
            } else {
                out.println("\nGame Over! You've run out of attempts.\nThe word was: " + wordToGuess);
            }
        }

        private List<String> readWords(String filename) {
            List<String> wList = new ArrayList<>();
            try (Scanner fileScanner = new Scanner(new File(filename))) {
                while (fileScanner.hasNextLine()) {
                    wList.add(fileScanner.nextLine().trim());
                }
            } catch (FileNotFoundException e) {
                System.out.println("File " + filename + " not found, inserting backups.");
                wList.addAll(Arrays.asList("apple", "banana", "computer", "java", "network"));
            }
            return wList;
        }
    }
}