package com.manabrew.network;

import com.manabrew.inventory.*;
import com.manabrew.model.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static Pantry mainPantry = new Pantry();
    public static StorageBunker<OrderTicket> activeOrders = new StorageBunker<>();
    public static int totalGold = 0;
    
    private static CopyOnWriteArrayList<ClientHandler> activePlayers = new CopyOnWriteArrayList<>();
    private static final String[] ALL_RECIPES = {"healing", "toxic", "fireball", "mana"};
    private static Random rand = new Random();

    public static synchronized void addGold(int amount) {
        totalGold += amount;
    }

    //clean way to add fully logged-in players to the broadcast list
    public static void addPlayer(ClientHandler player) {
        activePlayers.add(player);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Tavern Server Open! Waiting for alchemists...");
            
            startGameLoop(); 

            while (true) {
                Socket client = serverSocket.accept();
                ClientHandler newPlayer = new ClientHandler(client, mainPantry, activeOrders);
                new Thread(newPlayer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler ch : activePlayers) {
            ch.sendMessage(message);
        }
    }

    private static void startGameLoop() {
        new Thread(() -> {
            long lastOrderTime = System.currentTimeMillis();
            
            while (true) {
                try {
                    Thread.sleep(1000); 
                    
                    for (OrderTicket ticket : activeOrders.getSnapshot()) {
                        ticket.tickTimer();
                        if (ticket.getTimeLeft() <= 0) {
                            activeOrders.remove(ticket);
                            broadcast(TerminalColors.RED + "\n[ ALERT ] Order Failed! Customers are angry over missed: " 
                                + ticket.getPotion().getName() + TerminalColors.RESET);
                        } else if (ticket.getTimeLeft() == 10) {
                            broadcast(TerminalColors.YELLOW + "\n[ ! ] Hurry! " + ticket.getPotion().getName() 
                                + " expires in 10 seconds!" + TerminalColors.RESET);
                        }
                    }

                    long now = System.currentTimeMillis();
                    if (now - lastOrderTime > 15000) {
                        lastOrderTime = now;
                        String randomType = ALL_RECIPES[rand.nextInt(ALL_RECIPES.length)];
                        Potion newOrder = Potion.create(randomType);
                        
                        OrderTicket ticket = new OrderTicket(newOrder, 45);
                        activeOrders.add(ticket);
                        
                        broadcast(TerminalColors.GREEN + "\n==============================" + TerminalColors.RESET);
                        broadcast(TerminalColors.GREEN + "[ NEW TICKET ] " + newOrder.getName() + "!" + TerminalColors.RESET);
                        broadcast(TerminalColors.YELLOW + " > Required: " + newOrder.getRecipeString() + TerminalColors.RESET);
                        broadcast(TerminalColors.CYAN + " > Time Limit: 45s" + TerminalColors.RESET);
                        broadcast(TerminalColors.GREEN + "==============================" + TerminalColors.RESET);
                    }
                } catch (InterruptedException e) { }
            }
        }).start();
    }
}