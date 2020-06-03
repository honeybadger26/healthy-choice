package com.mycompany.app;

import java.util.Scanner;

import com.mycompany.app.models.Recipe;
import com.mycompany.app.models.Ingredient;
import com.mycompany.app.models.Instruction;

/**
 * Used to print options and other common 
 * input/output operations
 */
public class UI {
    /**
     * Get an integer from user. 
     * Prints a prompt beforehand
     */
    public int inputInt(String prompt) {
        System.out.print(prompt);

        Scanner sc = new Scanner(System.in);

        // keep trying until valid int is given
        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Error: Invalid option. Try again> ");
        }

        return sc.nextInt();
    }

    /**
     * Get and integer from user that is in the given range. 
     * Prints a prompt beforehand. Uses inputInt above.
     */
    public int inputRange(String prompt, int min, int max) {
        int option = -1;

        // keep trying until number is in range
        while (true) {
            option = this.inputInt(prompt);
            
            if (min <= option && option <= max) return option;
            
            System.out.print("Error: Invalid option. Try again> ");
        }
    }

    /**
     * Get string from user. Prints a prompt beforehand
     */
    public String inputString(String prompt) {
        System.out.print(prompt);

        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    /**
     * Clear the terminal output
     */
    public void clearScreen() {
        System.out.print("\u001b[2J");
        System.out.flush();
    }

    /**
     * Format recipes nicely given a list of recipes
     * Used to list a particular days recipes.
     */
    public void listRecipes(Recipe[] recipes) {
        for (int i = 0; i < recipes.length; i++) {
            System.out.printf("  %d. %s\n", i, recipes[i].getName());
        }
    }

    /**
     * Print the details for a given recipe
     */
    public void printRecipe(Recipe recipe) {
        // Print recipe name
        System.out.printf("\n'%s'\n", recipe.getName());

        // Print ingredients
        System.out.println("Ingredients:");
        for (Ingredient ingredient : recipe.getIngredients()) {
            System.out.printf("  %s\n", ingredient.getText());
        }

        // Print instructions
        System.out.println("Instructions:");
        for (Instruction instructions : recipe.getInstructions()) {
            System.out.printf("  %d. %s\n", instructions.getOrder(), instructions.getText());
        }
    }
}