package com.mycompany.app;

import com.mycompany.app.datamodels.Recipe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Scanner;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONArray;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Middleware {
    OkHttpClient client;

    public Middleware() {
        this.client = new OkHttpClient();
    }

    public Recipe[] getRecipes(String query) {
        // encode string query
        String encodedQuery = null;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // query url
        String url = "https://tasty.p.rapidapi.com/recipes/list?q=" + encodedQuery + "&from=0&sizes=20";

        // build api request todo: move to own function
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-host", "tasty.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "68758c6623msh2f10f92bbb6a9a9p1ad69djsne588b8d75367")
            .build();

        // query api
        String responseString = null;
        try {
            System.out.print("Querying api...");
            Response jsonResponse = client.newCall(request).execute();
            responseString = jsonResponse.body().string();
            System.out.print("done\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // exctract api response
        JSONObject response = new JSONObject(responseString);
        JSONArray results = response.getJSONArray("results");

        Recipe[] recipes = new Recipe[0];

        for (int i = 0; i < results.length(); i++) {
            recipes = this.addRecipe(recipes, results.getJSONObject(i));
        }

        return recipes;
    }

    /**
     * Decode recipe and add it to given recipe array
     */
    private Recipe[] addRecipe(Recipe[] recipes, JSONObject recipe) {
        if (recipe.has("recipes")) {
            JSONArray nestedRecipes = recipe.getJSONArray("recipes");
            Integer nestedRecipesLen = nestedRecipes.length();

            Recipe[] newRecipes = new Recipe[nestedRecipesLen+1];
            for (int i = 0; i < nestedRecipesLen; i++) {
                newRecipes = this.addRecipe(recipes, nestedRecipes.getJSONObject(i));
            }
            return newRecipes;
        }

        String name = recipe.getString("name");

        // add ingredients
        JSONArray JSONIngredients = recipe.getJSONArray("sections").getJSONObject(0).getJSONArray("components");
        String[] ingredients = new String[JSONIngredients.length()];
        for (int i = 0; i < JSONIngredients.length(); i++) {
            ingredients[i] = JSONIngredients.getJSONObject(i).getString("raw_text");
        }

        // add instructions
        JSONArray JSONInstructions = recipe.getJSONArray("instructions");
        String[] instructions = new String[JSONInstructions.length()];
        for (int i = 0; i < JSONInstructions.length(); i++) {
            instructions[i] = JSONInstructions.getJSONObject(i).getString("display_text");
        }

        int recipeLen = recipes.length;

        Recipe[] newRecipes = new Recipe[recipeLen+1];
        for (int i = 0; i < recipeLen; i++) {
            newRecipes[i] = recipes[i];
        }
        newRecipes[recipeLen] = new Recipe(null, name, ingredients, instructions);

        return newRecipes;
    }
}
