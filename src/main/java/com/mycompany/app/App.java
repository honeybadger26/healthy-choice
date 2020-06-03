package com.mycompany.app;

import com.mycompany.app.models.Recipe;
import com.mycompany.app.database.SQLiteDB;
import com.mycompany.app.UI;
import com.mycompany.app.Middleware;

// apis: https://rapidapi.com/blog/recipe-apis/


public class App {
    public static void main(String[] args) {
        // define constants
        final int NUM_DAYS = 3;

        // create database instance
        SQLiteDB db = new SQLiteDB();

        // initialise the ui class
        UI ui = new UI();

        // init middleware
        Middleware middleware = new Middleware();

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
            // list recipes for each of the 3 days
            for (int i = 0; i < NUM_DAYS; i++) {
                System.out.printf("Day %d", i + 1);

                Recipe[] recipes = db.getRecipesForDay(i + 1);

                if (recipes.length == 0) {
                    System.out.print(" - no recipes\n");
                } else {
                    System.out.print("\n");
                    for (Recipe recipe : recipes) {
                        System.out.printf("  (%d) %s\n", recipe.getId(), recipe.getName());
                    }
                }

            }

            // get initial option
            System.out.print("\nWhat would you like to do?\n");
            for (int i = 0; i < options.length; i++) {
                System.out.printf("  %d. %s\n", i, options[i]);
            }

            int option = ui.inputRange("\nEnter a number> ", 0, options.length-1);

            int daynum = -1;
            int recipeid = -1;

            switch (option) {
                case 0: // add new recipe
                    daynum = ui.inputRange("For which day? ", 1, NUM_DAYS);

                    Recipe[] recipes = null;

                    do {
                        // query api for recipes
                        String query = ui.inputString("Enter search query> ");
                        recipes = middleware.getRecipes(query);

                        // list the found recipes
                        ui.listRecipes(recipes);
                    } while (recipes.length == 0);


                    // get recipe to add
                    option = ui.inputRange("\nEnter recipe number to add> ", 0, recipes.length-1);

                    // add recipe to database
                    db.addRecipeToDay(daynum, recipes[option]);

                    break;
                case 1: // remove recipe
                    daynum = ui.inputRange("For which day? ", 1, NUM_DAYS);
                    recipeid = ui.inputInt("For which recipe? Enter recipe id from above> ");

                    db.removeRecipeFromDay(daynum, recipeid);

                    break;
                case 2: // view details of a recipe
                    // todo: view recipe details like ingredients and instructions
                    recipeid = ui.inputInt("Enter recipe id (next to recipe name above)> ");

                    // fetch recipe from database
                    Recipe recipe = db.getRecipe(recipeid);

                    // print recipe details
                    if (recipe != null) ui.printRecipe(recipe);

                    break;
                case 3: // fill the days with random recipes based on a given query
                    break;
                case 4: // clear all the recipes in the plan
                    String confirmation = ui.inputString(
                        "This will delete all recipes over all days. Are you sure?\n" + 
                        "Enter 'yes' to continue or anything else to abort> "
                    );

                    if (confirmation == "yes") db.clearAllRecipes();

                    break;
                case 5: // quit program
                    return;
                default:
                    break;
            }

            ui.inputString("Press ENTER to continue...");
            ui.clearScreen();
        }
    }
}
