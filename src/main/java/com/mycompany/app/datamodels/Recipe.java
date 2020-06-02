package com.mycompany.app.datamodels;

/**
 * Recipe class to hold instructions and ingredients
 */
public class Recipe {
    private Integer id;
    private String name;
    private String[] ingredients;
    private String[] instructions;

    public Recipe(Integer id, String name, String[] ingredients, String[] instructions) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String[] getIngredients() {
        return this.ingredients;
    }

    public String[] getInstructions() {
        return this.instructions;
    }
}
