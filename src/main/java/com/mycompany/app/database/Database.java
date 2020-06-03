package com.mycompany.app.database;

import com.mycompany.app.models.Recipe;

/**
 * Interface to define the required database methods
 */
public interface Database {
    public void addRecipeToDay(int daynum, Recipe recipe);
    public void removeRecipeFromDay(int daynum, int recipeid);
    public Recipe[] getRecipesForDay(int daynum);

    public Recipe getRecipe(int recipeid);

    public void clearAllRecipes();

    public void cleanup();
}
