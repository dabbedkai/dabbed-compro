package com.manabrew.inventory;

import com.manabrew.model.ingredient; // pulling from the model folder
import java.util.HashMap;

public class Pantry {
    private HashMap<String, Integer> stock = new HashMap<>();

    public Pantry() {
        stock.put("water", 100);
        stock.put("dragon scale", 2); 
        stock.put("fairy dust", 50);
        stock.put("fire pepper", 15); // new ingredient
    }

    // synchronized to prevent race conditions from multiple players
    public synchronized boolean takeingredients(Ingredient[] reqs) {
        // verify stock first
        for (Ingredient i : reqs) {
            if (stock.getOrDefault(i.getname(), 0) <= 0) {
                return false;
            }
        }
        // deduct stock
        for (Ingredient i : reqs) {
            stock.put(i.getname(), stock.get(i.getname()) - 1);
        }
        return true;
    }
}
