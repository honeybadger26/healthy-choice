package com.mycompany.app.models;

/**
 * Ingredient class to hold all the required ingredient data
 */
public class Ingredient {
    private int id;
    private String text;

    public Ingredient(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }
}
