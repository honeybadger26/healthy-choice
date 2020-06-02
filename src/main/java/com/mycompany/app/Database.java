package com.mycompany.app;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;

import com.mycompany.app.datamodels.Recipe;

public class Database {
    private Connection conn;
    private Statement stmt;

    public Database() {
        // define database varibles
        String url = "jdbc:sqlite:sqlite.db";

        // create database
        try {
            System.out.println("Creating database...");
            this.conn = DriverManager.getConnection(url);
            this.stmt = this.conn.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        this.createTables();

    }

    public void createTables() {
        // todo: remove dayrecipe
        //
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
            System.out.println("Creating tables...");
            for (String cmd : cmds) {
                this.stmt.execute(cmd);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addRecipe(int daynum, Recipe recipe) {
        System.out.println("Saving recipe '"+recipe.getName()+"'...");

        Integer recipeid = null;
        try {
            ResultSet rs = this.stmt.executeQuery(String.format(
                "SELECT recipeid FROM Recipes WHERE recipename='%d'",
                recipe.getName()
            ));
            if (rs.next()) {
                recipeid = rs.getInt("recipeid");
            }
            recipeid = rs.getInt("recipeid");
        } catch (SQLException e) { 
            System.out.println(e.getMessage()); 
            return;
        }

        if (recipeid == null) {
            // insert recipe and get the recipeid of newly inserted recipe
            try {
                this.stmt.execute(String.format(
                    "INSERT INTO Recipes VALUES (NULL, '%s');",
                    recipe.getName()
                ));
                ResultSet rs = this.stmt.getGeneratedKeys();
                if (rs.next()) {
                    recipeid = (int) rs.getLong(1);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return;
            }

            // insert ingredients and instructions for recipe
            this.addIngredients(recipeid, recipe.getIngredients());
            this.addInstructions(recipeid, recipe.getInstructions());
        }

        try {
            this.stmt.execute(String.format(
                "INSERT INTO DayRecipe VALUES (NULL, %d, %d);",
                daynum, recipeid
            ));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeRecipe(int daynum, Integer recipeid) {
        String cmd = String.format(
            "DELETE FROM DayRecipe WHERE (daynum=%d AND recipeid=%d);",
            daynum, recipeid
        );

        try {
            System.out.println("Removing recipe...");
            this.stmt.execute(cmd);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Recipe[] getRecipes(int daynum) {
        Integer numRecipes = null;
        String cmd = String.format(
            "SELECT COUNT(*) as total FROM DayRecipe WHERE daynum=%s;",
            daynum
        );

        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            numRecipes = rs.getInt("total");
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
            return null;
        }

        Integer[] recipeids = new Integer[numRecipes];
        cmd = "SELECT * FROM DayRecipe WHERE daynum=" + daynum + ";";
        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            int i = 0;
            while(rs.next()) { 
                recipeids[i] = rs.getInt("recipeid");
                i += 1;
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
            return null;
        }

        Recipe[] recipes = new Recipe[numRecipes];
        for (int i = 0; i < numRecipes; i++) {
            recipes[i] = this.getRecipe(recipeids[i]);
        }

        return recipes;
    }

    public Recipe getRecipe(int recipeid) {
        String cmd = "SELECT * FROM Recipes WHERE recipeid=" + recipeid + ";";
        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            if(rs.next()) { 
                String recipename = rs.getString("recipename");
                String[] instructions = this.getInstructions(recipeid);
                String[] ingredients = this.getIngredients(recipeid);

                return new Recipe(recipeid, recipename, ingredients, instructions);
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        return null;
    }

    private void addInstructions(int recipeid, String[] instructions) {
        System.out.println("Saving instructions...");
        for (int i = 0; i < instructions.length; i++) {
            String cmd = "INSERT INTO Instructions VALUES (\n" +
                "   NULL,\n" + 
                "   '" + instructions[i] + "',\n" + 
                "   " + Integer.toString(i) + ",\n" + 
                "   " + recipeid + "\n" + 
                ");";

            try {
                this.stmt.execute(cmd);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String[] getInstructions(int recipeid) {
        Integer numInstructions = null;
        String cmd = "SELECT COUNT(*) as total FROM Instructions WHERE recipeid=" + recipeid + ";";

        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            numInstructions = rs.getInt("total");
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        String[] instructions = new String[numInstructions];
        cmd = "SELECT * FROM Instructions WHERE recipeid=" + recipeid + ";";
        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            int i = 0;
            while(rs.next()) { 
                String instruction = rs.getString("instructiontext");
                instructions[i] = instruction;
                i += 1;
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        return instructions;
    }

    private void addIngredients(int recipeid, String[] ingredients) {
        System.out.println("Saving ingredients...");
        for (int i = 0; i < ingredients.length; i++) {
            String cmd = "INSERT INTO Ingredients VALUES (\n" +
                "   NULL,\n" + 
                "   '" + ingredients[i] + "',\n" + 
                "   " + recipeid + "\n" + 
                ");";

            try {
                this.stmt.execute(cmd);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String[] getIngredients(int recipeid) {
        Integer numIngredients = null;
        String cmd = "SELECT COUNT(*) as total FROM Ingredients WHERE recipeid=" + recipeid + ";";

        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            numIngredients = rs.getInt("total");
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        String[] ingredients = new String[numIngredients];
        cmd = "SELECT * FROM Ingredients WHERE recipeid=" + recipeid + ";";
        try {
            ResultSet rs = this.stmt.executeQuery(cmd);
            int i = 0;
            while(rs.next()) { 
                String ingredient = rs.getString("ingredienttext");
                ingredients[i] = ingredient;
                i += 1;
            }
        } catch (SQLException e) { 
            System.out.println(e.getMessage());
        }

        return ingredients;
    }
}
