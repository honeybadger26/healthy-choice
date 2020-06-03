package com.mycompany.app.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;

import com.mycompany.app.models.Recipe;
import com.mycompany.app.models.Ingredient;
import com.mycompany.app.models.Instruction;

/**
 * SQLite implementation of the database
 * This file is huuuge :(
 */
public class SQLiteDB implements Database {
    private Connection conn;

    public SQLiteDB() {
        System.out.println("Creating database...");

        // define constants
        final String url = "jdbc:sqlite:sqlite.db";

        // create database
        try {
            this.conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        this.createTables();
    }

    /**
     * Create the necessary tables and define the schema
     */
    private void createTables() {
        System.out.print("Creating tables...");

        String[] cmds = { 
            "CREATE TABLE IF NOT EXISTS Recipes (\n" +
            "   recipeid integer PRIMARY KEY AUTOINCREMENT,\n" +
            "   recipename text NOT NULL\n" +
            ");",
            "CREATE TABLE IF NOT EXISTS Instructions (\n" +
            "   instructionid integer PRIMARY KEY AUTOINCREMENT,\n" +
            "   instructiontext text NOT NULL,\n" +
            "   instructionorder integer NOT NULL,\n" +
            "   recipeid integer NOT NULL,\n" +
            "   FOREIGN KEY(recipeid) REFERENCES Recipes(recipeid)\n" +
            ");",
            "CREATE TABLE IF NOT EXISTS Ingredients (\n" +
            "   ingredientid integer PRIMARY KEY AUTOINCREMENT,\n" +
            "   ingredienttext text NOT NULL,\n" +
            "   recipeid integer NOT NULL,\n" +
            "   FOREIGN KEY(recipeid) REFERENCES Recipes(recipeid)\n" +
            ");",
            "CREATE TABLE IF NOT EXISTS DayRecipe (\n" +
            "   dayrecipeid integer PRIMARY KEY AUTOINCREMENT,\n" +
            "   daynum integer NOT NULL,\n" +
            "   recipeid integer NOT NULL,\n" +
            "   FOREIGN KEY(recipeid) REFERENCES Recipes(recipeid)\n" +
            ");"
        };

        for (String cmd : cmds) {
            try {
                this.conn.createStatement().execute(cmd);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.print("done\n");
    }


    /**
     * Add a recipe to a given day
     */
    @Override
    public void addRecipeToDay(int daynum, Recipe recipe) {
        System.out.println("Adding recipe to day...");

        // add to Recipes table
        int recipeid = this.addRecipe(recipe);

        try {
            this.conn.createStatement().execute(String.format(
                "INSERT INTO DayRecipe VALUES (NULL, %d, %d);",
                daynum, recipeid
            ));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Remove a single recipe from a given day
     */
    @Override
    public void removeRecipeFromDay(int daynum, int recipeid) {
        System.out.println("Removing recipe from day...");

        // remove the first recipe enountered
        // with given recipeid and daynum
        try {
            this.conn.createStatement().execute(String.format(
                "DELETE FROM DayRecipe WHERE dayrecipeid IN (\n" +
                "   SELECT dayrecipeid FROM DayRecipe WHERE (daynum=%d AND recipeid=%d) LIMIT 1\n" + 
                ");\n",
                daynum, recipeid
            ));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // check if recipeid not in DayRecipe. if not, then safe to 
        // delete from recipes table as it is no longer being used.
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT COUNT(*) as total FROM DayRecipe WHERE recipeid=%d;\n",
                recipeid
            ));

            if (rs.getInt("total") == 0) {
                System.out.println("Recipe not used elsewhere.");
                this.removeRecipe(recipeid);
            };
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Get all the recipes for a given day
     */
    @Override
    public Recipe[] getRecipesForDay(int daynum) {
        // first get the number of recipes in the given day.
        // this is used to make the recipes array below
        int numRecipes = -1;
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT COUNT(*) as total FROM DayRecipe WHERE daynum=%d;",
                daynum
            ));

            numRecipes = rs.getInt("total");
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
            return null;
        }

        // build array of recipes
        Recipe[] recipes = new Recipe[numRecipes];
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT * FROM DayRecipe WHERE daynum=%d;",
                daynum
            ));
            
            int i = 0;
            while (rs.next()) {
                recipes[i] = this.getRecipe(rs.getInt("recipeid"));
                i++;
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
            return null;
        }

        return recipes;
    }

    /**
     * Get single recipe by its id. If not found
     * return null
     */
    @Override
    public Recipe getRecipe(int recipeid) {
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT * FROM Recipes WHERE recipeid=%d;",
                recipeid
            ));

            if (rs.next()) { 
                String recipename = rs.getString("recipename");
                Ingredient[] ingredients = this.getIngredients(recipeid);
                Instruction[] instructions = this.getInstructions(recipeid);
                return new Recipe(recipeid, recipename, ingredients, instructions);
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        return null;
    }

    /**
     * Remove all recipes.
     * This clears the whole database.
     */
    @Override
    public void clearAllRecipes() {
        System.out.print("Removing all data...");

        String[] cmds = {
            "DELETE FROM Recipes;\n",
            "DELETE FROM Instructions;\n",
            "DELETE FROM Ingredients;\n",
            "DELETE FROM DayRecipe;"
        };

        for (String cmd : cmds) {
            try {
                this.conn.createStatement().execute(cmd);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.print("done\n");

    }

    /**
     * Close the databse connnection
     */
    @Override
    public void cleanup() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int addRecipe(Recipe recipe) {
        System.out.println("Saving recipe to database...");

        // check if the recipe is already in the ...
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT recipeid FROM Recipes WHERE recipename='%s';",
                recipe.getName()
            ));

            if (rs.next()) {
                System.out.println("Recipe found in database. No need to add");
                return rs.getInt("recipeid");
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        // ... and if not, add it
        try {
            this.conn.createStatement().execute(String.format(
                "INSERT INTO Recipes VALUES (NULL, '%s');",
                recipe.getName()
            ));

            ResultSet rs = this.conn.createStatement().getGeneratedKeys();

            if (rs.next()) {
                int recipeid = (int) rs.getLong(1);

                // Add ingredients and instructions for the recipe
                this.addIngredients(recipeid, recipe.getIngredients());
                this.addInstructions(recipeid, recipe.getInstructions());

                return recipeid;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return -1;
    }

    /**
     * Remove a single recipe from the Recipes table
     */
    private void removeRecipe(int recipeid) {
        System.out.println("Removing recipe from database...");

        try {
            this.conn.createStatement().execute(String.format(
                "DELETE FROM Recipes WHERE recipeid=%d;",
                recipeid
            ));

            // need to remove associated ingredients and instructions
            this.removeIngredients(recipeid);
            this.removeInstructions(recipeid);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Add a list of instructions for a recipe
     */
    private void addInstructions(int recipeid, Instruction[] instructions) {
        System.out.print("Saving instructions...");

        for (Instruction instruction : instructions) {
            try {
                String text = instruction.getText();
                int order = instruction.getOrder();

                this.conn.createStatement().execute(String.format(
                    "INSERT INTO Instructions VALUES (NULL, '%s', %d, %d);",
                    text, order, recipeid
                ));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.print("done\n");
    }

    /**
     * Remove all the instructions related to a recipe
     */
    private void removeInstructions(int recipeid) {
        System.out.print("Removing instructions...");

        try {
            this.conn.createStatement().execute(String.format(
                "DELETE FROM Instructions WHERE recipeid=%d;",
                recipeid
            ));
            System.out.print("done\n");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Get an ordered list of instructions for a given recipe
     */
    private Instruction[] getInstructions(int recipeid) {
        // find how many instructions there are for the 
        // recipe. used to build array below
        int numInstructions = -1;
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT COUNT(*) as total FROM Instructions WHERE recipeid=%d;",
                recipeid
            ));

            numInstructions = rs.getInt("total");
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        // build instructions arrray
        Instruction[] instructions = new Instruction[numInstructions];
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT * FROM Instructions WHERE recipeid=%d ORDER BY instructionorder ASC;",
                recipeid
            ));

            int i = 0;
            while(rs.next()) { 
                int id      = rs.getInt("instructionid");
                String text = rs.getString("instructiontext");
                int order   = rs.getInt("instructionorder");

                instructions[i] = new Instruction(id, text, order);

                i += 1;
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        return instructions;
    }

    /**
     * Add a list of ingredients for a recipe.
     * Similar to addInstructions
     */
    private void addIngredients(int recipeid, Ingredient[] ingredients) {
        System.out.print("Saving ingredients...");

        for (Ingredient ingredient : ingredients) {
            try {
                this.conn.createStatement().execute(String.format(
                    "INSERT INTO Ingredients VALUES (NULL, '%s', %d);",
                    ingredient.getText(), recipeid
                ));
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.print("done\n");
    }

    /**
     * Remove all the ingredients for a recipe.
     * Similar to removeInstructions
     */
    private void removeIngredients(int recipeid) {
        System.out.print("Removing ingredients...");

        try {
            this.conn.createStatement().execute(String.format(
                "DELETE FROM Ingredients WHERE recipeid=%d;",
                recipeid
            ));
            System.out.print("done\n");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Get a list of all the ingredients for a recipe.
     * Similar to getInstructions
     */
    private Ingredient[] getIngredients(int recipeid) {
        // find number of ingredients the recipe has.
        // used to build ingredients array below
        int numIngredients = -1;
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT COUNT(*) as total FROM Ingredients WHERE recipeid=%d;",
                recipeid
            ));

            numIngredients = rs.getInt("total");
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        // build ingredients of array
        Ingredient[] ingredients = new Ingredient[numIngredients];
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT * FROM Ingredients WHERE recipeid=%d;",
                recipeid
            ));

            int i = 0;
            while(rs.next()) {
                int id      = rs.getInt("ingredientid");
                String text = rs.getString("ingredienttext");

                ingredients[i] = new Ingredient(id, text);

                i += 1;
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        return ingredients;
    }
}
