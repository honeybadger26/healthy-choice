package com.mycompany.app.models;

/**
 * Instruction class to hold instructions text and order
 */
public class Instruction {
    private int id;
    private String text;
    private int order;

    public Instruction(int id, String text, int order) {
        this.id = id;
        this.text = text;
        this.order = order;
    }

    public int getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public int getOrder() {
        return this.order;
    }
}
