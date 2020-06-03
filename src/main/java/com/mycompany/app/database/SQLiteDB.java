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
 */
public class SQLiteDB implements Database {
    private Connection conn;

    public SQLiteDB() {
        System.out.println("Creating database...");

        // define database varibles
        String url = "jdbc:sqlite:sqlite.db";

        // create database
        try {
            this.conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        this.createTables();
    }

    /**
     * Create the necessary tables
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

        try {
            for (String cmd : cmds) {
                this.conn
                    .createStatement()
                    .execute(cmd);
            }
            System.out.print("done\n");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Add a recipe to a given day
     */
    @Override
    public void addRecipeToDay(int daynum, Recipe recipe) {
        System.out.println("Adding recipe to day...");

        // todo: move this to addRecipe
        // return value of -1 should mean found in db

        int recipeid = -1;
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT recipeid FROM Recipes WHERE recipename='%s';",
                recipe.getName()
            ));

            if (rs.next()) {
                recipeid = rs.getInt("recipeid");
                System.out.println("Recipe found in database");
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage()); 
            return;
        }

        // Add recipe if not currently stored in database
        if (recipeid == -1) {
            System.out.println("Recipe not found in database");
            recipeid = this.addRecipe(recipe);

            if (recipeid == -1) {
                System.out.println("Error: Could not save new recipe");
                return;
            }
        }

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
        System.out.print("Removing recipe from day...");

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

        // todo: check if recipeid not in dayrecipe. then safe to 
        // delete from recipes table

        System.out.print("done\n");
    }


    /**
     * Get all the recipe for a given day
     */
    @Override
    public Recipe[] getRecipesForDay(int daynum) {
        int numRecipes = -1;
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(String.format(
                "SELECT COUNT(*) as total FROM DayRecipe WHERE daynum=%d;",
                daynum
            ));

            numRecipes = rs.getInt("total");
            
            System.out.println(numRecipes);
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
            return null;
        }

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
     * Get single recipe by its id
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
     * Clear all recipes in the database.
     * This essentially clears all data.
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
    public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int addRecipe(Recipe recipe) {
        System.out.println("Saving recipe to database...");

        int recipeid = -1;
        try {
            this.conn.createStatement().execute(String.format(
                "INSERT INTO Recipes VALUES (NULL, '%s');",
                recipe.getName()
            ));

            ResultSet rs = this.conn.createStatement().getGeneratedKeys();

            if (rs.next()) {
                recipeid = (int) rs.getLong(1);

                // Insert ingredients and instructions for the recipe
                this.addIngredients(recipeid, recipe.getIngredients());
                this.addInstructions(recipeid, recipe.getInstructions());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return recipeid;
    }

    /**
     * Remove a single recipe from the Recipes table
     */
    private void removeRecipe(int recipeid) {
        System.out.print("Removing recipe from database...");

        try {
            this.conn.createStatement().execute(String.format(
                "DELETE FROM Recipes WHERE recipeid=%d;",
                recipeid
            ));
            System.out.print("done\n");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Add an list of instructions for a recipe
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
     * Add a list of ingredients for a recipe
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
     * Remove all the ingredients for a recipe
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
     * Get a list of all the ingredients for a recipe
     */
    private Ingredient[] getIngredients(int recipeid) {
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
