package com.mycompany.app;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
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
    @Test
    public void a() {
        SQLiteDB db = new SQLiteDB();
        Middleware middleware = new Middleware();
        UI ui = new UI();

        final String QUERY = "chicken";
        final int DAY = 1;

        int numInitial = db.getRecipesForDay(DAY).length;

        Recipe[] fetchedRecipes = middleware.getRecipes(QUERY);
        db.addRecipeToDay(DAY, fetchedRecipes[0]);
        db.addRecipeToDay(DAY, fetchedRecipes[0]);

        Recipe[] savedRecipes = db.getRecipesForDay(DAY);
        int recipeId = savedRecipes[0].getId();     // for deleting below
        int numFinal = savedRecipes.length;

        assertEquals(numInitial + 2, numFinal);

        ui.listRecipes(savedRecipes);

        numInitial = numFinal;

        db.removeRecipeFromDay(DAY, recipeId);
        db.removeRecipeFromDay(DAY, recipeId);

        numFinal = db.getRecipesForDay(DAY).length;

        db.close();

        assertEquals(numInitial - 2, numFinal);
    }

    @Test
    public void b() {
        SQLiteDB db = new SQLiteDB();
        Middleware middleware = new Middleware();

        db.clearAllRecipes();
        

        assertEquals(0, db.getRecipesForDay(1).length);
        assertEquals(0, db.getRecipesForDay(2).length);
        assertEquals(0, db.getRecipesForDay(3).length);
        
        db.close();
    }
}