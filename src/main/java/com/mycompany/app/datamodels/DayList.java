package com.mycompany.app.datamodels;

import com.mycompany.app.datamodels.Recipe;

/**
 * Day class to hold recipes
 */
public class DayList {
    private int dayNum;
    private Recipe[] recipes;

    public DayList(int dayNum, Recipe[] recipes) {
        this.dayNum = dayNum;
        this.recipes = recipes;
    }

    public int getDayNum() {
        return this.dayNum;
    }

    public Recipe[] getRecipes() {
        return this.recipes;
    }
}
