package com.manabrew.model;
public abstract class Potion implements Brewable {
    protected String name;
    protected int tier;
    protected int price;
    protected Ingredient[] recipe;

    public Potion(String name, int tier, int price, Ingredient[] recipe) {
        this.name = name;
        this.tier = tier;
        this.price = price;
        this.recipe = recipe;
    }

    public String getname() { return name; }
    public int getprice() { return price; }
    public int gettier() { return tier; }
    public Ingredient[] getrecipe() { return recipe; }


public static Potion create(String type) {
        switch (type.toLowerCase()) {
            case "healing": return new HealingElixir();
            case "toxic": return new ToxicBrew();
            case "fireball": return new FireballPotion();
            case "mana": return new ManaCrystal();
            default: return null;
        }
    }
}
