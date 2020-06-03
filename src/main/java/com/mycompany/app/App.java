package com.mycompany.app;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.lang.Math;

import com.mycompany.app.models.Recipe;
import com.mycompany.app.database.SQLiteDB;
import com.mycompany.app.UI;
import com.mycompany.app.Middleware;

// apis: https://rapidapi.com/blog/recipe-apis/


public class App {
    // define constants
    private final int NUM_DAYS = 3;
    // this is ONLY used when filling
    // based on query. otherwise can add as
    // many recipes as you want
    private final int MAX_RECIPES_PER_DAY = 3;

    private SQLiteDB db;
    private UI ui;
    private Middleware middleware;


    public static void main(String[] args) {
        App app = new App();

        app.startup();

        String[] options = {
            "Add new recipe", 
            "Remove recipe", 
            "View recipe", 
            "Fill based on query",
            "Clear all recipes",
            "Quit"
        };

        System.out.println();

        while (true) {
            // print the current plan
            app.printPlan();

            // print available options
            System.out.print("\nWhat would you like to do?\n");
            for (int i = 0; i < options.length; i++) {
                System.out.printf("  %d. %s\n", i, options[i]);
            }

            // get option from user
            int option = app.ui.inputRange("\nEnter a number> ", 0, options.length-1);

            switch (option) {
                case 0:
                    app.addRecipeToPlan();
                    break;
                case 1:
                    app.removeRecipeFromPlan();
                    break;
                case 2:
                    app.viewRecipeDetails();
                    break;
                case 3:
                    app.fillPlan();
                    break;
                case 4:
                    app.clearPlan();
                    break;
                case 5: 
                    // quit program
                    return;
                default:
                    break;
            }

            app.ui.inputString("\nPress ENTER to continue...");
            app.ui.clearScreen();
        }
    }

    /**
     * Initialise the necessary vars
     */
    public void startup() {
        this.db = new SQLiteDB();
        this.ui = new UI();
        this.middleware = new Middleware();
    }

    /**
     * List recipes for each of the days
     */
    public void printPlan() {
        for (int i = 0; i < this.NUM_DAYS; i++) {
            System.out.printf("Day %d", i + 1);

            Recipe[] recipes = this.db.getRecipesForDay(i + 1);

            if (recipes.length == 0) {
                System.out.print(" - no recipes\n");
            } else {
                System.out.print("\n");
                for (Recipe recipe : recipes) {
                    System.out.printf("  (%d) %s\n", recipe.getId(), recipe.getName());
                }
            }
        }
    }

    /**
     * Option for adding a new recipe to a particular day
     */
    public void addRecipeToPlan() {
        int daynum = this.ui.inputRange("For which day? ", 1, this.NUM_DAYS);

        Recipe[] recipes = null;

        do {
            // query api for recipes
            String query = this.ui.inputString("Enter search query> ");
            recipes = this.middleware.getRecipes(query);

            System.out.printf("\n%d recipes found\n", recipes.length);

            // list the found recipes
            this.ui.listRecipes(recipes);
        } while (recipes.length == 0);


        // get recipe to add
        int option = this.ui.inputRange("\nEnter recipe number to add> ", 0, recipes.length-1);

        // add recipe to database
        this.db.addRecipeToDay(daynum, recipes[option]);
    }

    /**
     * Option for removing a recipe from a day
     */
    public void removeRecipeFromPlan() {
        int daynum = this.ui.inputRange("For which day? ", 1, this.NUM_DAYS);
        int recipeid = this.ui.inputInt("For which recipe? Enter recipe id from above> ");

        this.db.removeRecipeFromDay(daynum, recipeid);
    }

    /**
     * Option for viewing the details of a recipe
     */
    public void viewRecipeDetails() {
        int recipeid = this.ui.inputInt("Enter recipe id (next to recipe name above)> ");

        // fetch recipe from database
        Recipe recipe = this.db.getRecipe(recipeid);

        // print recipe details
        if (recipe != null) {
            this.ui.printRecipe(recipe);
            return;
        }

        System.out.println("Recipe not found");
    }

    /**
     * Option for clearing plan and filling with
     * recipes related to given query
     */
    public void fillPlan() {
        if (this.clearPlan()) {
            db.clearAllRecipes();

            String query = ui.inputString("Enter search query> ");
            Recipe[] recipes = middleware.getRecipes(query);

            System.out.printf("\n%d recipes found\n", recipes.length);

            List<Recipe> shuffledRecipes = Arrays.asList(recipes);
            Collections.shuffle(shuffledRecipes);

            // will try to fill each day with MAX_RECIPES_PER_DAY recipes.
            // if not enough recipes available then will even out recipes
            // over the days as much as possible
            int recipesToAdd = Math.min(shuffledRecipes.size(), this.NUM_DAYS*this.MAX_RECIPES_PER_DAY);
            for (int i = 0; i < recipesToAdd; i++) {
                int daynum = i % NUM_DAYS + 1; 
                db.addRecipeToDay(daynum, shuffledRecipes.get(i));
            }
        }
    }

    /**
     * Option for clearing the whole plan and starting from scratch.
     * Also used above when filling plan.
     */
    public boolean clearPlan() {
        String confirmation = ui.inputString(
            "This will delete all recipes over all days. Are you sure?\n" + 
            "Enter 'yes' to continue or anything else to abort> "
        );

        if (confirmation.equals("yes")) {
            db.clearAllRecipes();
            return true;
        }

        return false;
    }
}
