package com.manabrew.model;

public interface Brewable {
    // default method for time calculation
    default int calculatebrewtime(int tier) {
        return tier * 2;
    }

    // static utility to check explosions
    static boolean isvolatile(Ingredient a, Ingredient b) {
        return (a.getname().equals("dragon scale") && b.getname().equals("fairy dust")) ||
               (a.getname().equals("fairy dust") && b.getname().equals("dragon scale"));
    }
}