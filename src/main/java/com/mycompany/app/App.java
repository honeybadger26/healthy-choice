package com.mycompany.app;

import com.mycompany.app.datamodels.Recipe;
import com.mycompany.app.datamodels.DayList;
import com.mycompany.app.Database;
import com.mycompany.app.UI;
import com.mycompany.app.Middleware;

// apis: https://rapidapi.com/blog/recipe-apis/

public class App {
    public static void main( String[] args ) {
        // create database instance
        Database db = new Database();

        // initialise the ui class
        UI ui = new UI();

        // init middleware
        Middleware middleware = new Middleware();

        String[] options = {
            "Add new recipe", 
            "Remove recipe", 
            "View recipe", 
            "Quit"
        };

        while (true) {
            // list all days and recipes
            DayList[] daylist = new DayList[3];

            for (Integer i = 1; i <= 3; i++) {
                daylist[i-1] = new DayList(i, db.getRecipes(i));

                System.out.println("Day " + Integer.toString(i));

                for (Recipe recipe : daylist[i-1].getRecipes()) {
                    System.out.println("\t" + recipe.getId() + ". " + recipe.getName());
                }
            }

            // get initial option
            System.out.println("What would you like to do?");
            for (int i = 0; i < options.length; i++) {
                System.out.println(Integer.toString(i) + ". " + options[i]);
            }

            Integer option = ui.inputRange("Enter a number> ", 0, options.length-1);

            Integer daynum = null;
            Recipe[] recipes = null;

            switch(option) {
                case 0:
                    daynum = ui.inputRange("For which day? ", 1, 3);
                    String query = ui.inputString("Enter search query> ");
                    recipes = middleware.getRecipes(query);

                    System.out.println("Found " + recipes.length + " recipes");
                    for (int i = 0; i < recipes.length; i++) {
                        System.out.println(Integer.toString(i) + ". " + recipes[i].getName());
                    }

                    // get recipe to add
                    option = ui.inputRange("Enter recipe number to add> ", 0, recipes.length-1);

                    // add recipe to database
                    db.addRecipe(daynum, recipes[option]);

                    break;
                case 1:
                    daynum = ui.inputRange("For which day? ", 1, 3);

                    recipes = db.getRecipes(daynum);

                    // list recipes
                    for (int i = 0; i < recipes.length; i++) {
                        System.out.println(Integer.toString(i) + ". " + recipes[i].getName());
                    }

                    option = ui.inputRange("Enter recipe number to delete> ", 0, recipes.length-1);

                    // remove recipe
                    db.removeRecipe(daynum, recipes[option].getId());

                    break;
                case 2:
                    // todo: view recipe details like ingredients and instructions
                    break;
                case 3:
                    return;
                default:
                    break;
            }

            ui.inputString("Press ENTER to continue...");
            ui.clearScreen();
        }
    }
}
