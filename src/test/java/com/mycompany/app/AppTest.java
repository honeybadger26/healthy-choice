package com.mycompany.app;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.mycompany.app.database.SQLiteDB;
import com.mycompany.app.Middleware;
import com.mycompany.app.models.Recipe;
import com.mycompany.app.UI;

/**
 * Unit tests for app
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest {
    SQLiteDB db;
    Middleware middleware;

    /**
     * Run before tests are conducted
     */
    @Before
    public void starup() {
        this.db = new SQLiteDB();
        this.middleware = new Middleware();
    }

    /**
     * Test that adds and removes recipes
     */
    @Test
    public void a() {
        final String QUERY = "apple";
        final int DAY = 1;

        int numInitial = this.db.getRecipesForDay(DAY).length;

        // add some recipes based on above query
        Recipe[] fetchedRecipes = this.middleware.getRecipes(QUERY);
        this.db.addRecipeToDay(DAY, fetchedRecipes[0]);
        this.db.addRecipeToDay(DAY, fetchedRecipes[1]);

        Recipe[] savedRecipes = this.db.getRecipesForDay(DAY);
        int recipeId = savedRecipes[0].getId();     // for deleting below
        int numFinal = savedRecipes.length;

        // added 2 recipes so check the number of recipes has
        // changed correclty
        assertEquals(numInitial + 2, numFinal);

        numInitial = numFinal;

        // remove a single recipe
        this.db.removeRecipeFromDay(DAY, recipeId);

        numFinal = this.db.getRecipesForDay(DAY).length;

        // check removal worked correctly
        assertEquals(numInitial - 1, numFinal);
    }

    /**
     * Test to clear all the data in the database
     */
    @Test
    public void b() {
        final int NUM_DAYS = 3;

        this.db.clearAllRecipes();

        // make sure each day has been cleared
        for (int i = 0; i < NUM_DAYS; i++) {
            assertEquals(0, this.db.getRecipesForDay(i + 1).length);
        }
    }

    /**
     * Run after tests are completed
     */
    @After
    public void tearDown() {
        this.db.cleanup();
    }
}