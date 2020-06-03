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
    public int inputInt(String prompt) {
        System.out.print(prompt);

        Scanner sc = new Scanner(System.in);

        while (!sc.hasNextInt()) {
            sc.next();
            System.out.print("Error: Invalid option. Try again> ");
        }

        return sc.nextInt();
    }

    public int inputRange(String prompt, int min, int max) {
        int option = -1;

        while (true) {
            option = this.inputInt(prompt);
            
            if (min <= option && option <= max) {
                return option;
            }
            
            System.out.print("Error: Invalid option. Try again> ");
        }
    }

    public String inputString(String prompt) {
        System.out.print(prompt);

        Scanner sc = new Scanner(System.in);
        String result = sc.nextLine();

        return result;
    }

    public void clearScreen() {
        System.out.print("\u001b[2J");
        System.out.flush();
    }

    public void listRecipes(Recipe[] recipes) {
        System.out.printf("\n%d recipes found\n", recipes.length);

        for (int i = 0; i < recipes.length; i++) {
            System.out.printf("  %d. %s\n", i, recipes[i].getName());
        }
    }

    public void printRecipe(Recipe recipe) {
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