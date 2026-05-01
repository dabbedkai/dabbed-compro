package com.manabrew.inventory;

import java.util.ArrayList;

// generic class
public class StorageBunker<t> {
    private ArrayList<t> items = new ArrayList<>();

    public synchronized void add(t item) {
        items.add(item);
    }

    public synchronized t take() {
        if (items.isEmpty()) return null;
        return items.remove(0);
    }

    public synchronized int size() {
        return items.size();
    }
}
