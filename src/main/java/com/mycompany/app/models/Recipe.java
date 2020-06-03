package com.mycompany.app.models;

import com.mycompany.app.models.Ingredient;
import com.mycompany.app.models.Instruction;

/**
 * Recipe class that holds name, instructions and ingredients
 */
public class Recipe {
    private int id;
    private String name;
    private Ingredient[] ingredients;
    private Instruction[] instructions;

    public Recipe(int id, String name, Ingredient[] ingredients, Instruction[] instructions) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Ingredient[] getIngredients() {
        return this.ingredients;
    }

    public Instruction[] getInstructions() {
        return this.instructions;
    }
}
