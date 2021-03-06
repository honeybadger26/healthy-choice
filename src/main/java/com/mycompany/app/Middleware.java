package com.mycompany.app;

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

import com.mycompany.app.models.Ingredient;
import com.mycompany.app.models.Instruction;
import com.mycompany.app.models.Recipe;

/**
 * Handles all communication with api endpoint
 */
public class Middleware {
    OkHttpClient client;

    public Middleware() {
        this.client = new OkHttpClient();
    }

    /**
     * Make the request to get recipes from
     * api endpoint
     */
    public Recipe[] getRecipes(String query) {
        final String API_HOST = "tasty.p.rapidapi.com";
        final String API_KEY = "68758c6623msh2f10f92bbb6a9a9p1ad69djsne588b8d75367";

        System.out.print("Querying api...");

        // encode string query into proper format
        String encodedQuery = null;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // query url
        String url = "https://tasty.p.rapidapi.com/recipes/list?q=" + encodedQuery + "&from=0&sizes=20";

        // build api request
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("x-rapidapi-host", API_HOST)
            .addHeader("x-rapidapi-key", API_KEY)
            .build();

        // make the request to the api
        String responseString = null;
        try {
            Response jsonResponse = client.newCall(request).execute();
            responseString = jsonResponse.body().string();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // exctract results from api response
        JSONObject response = new JSONObject(responseString);
        JSONArray results = response.getJSONArray("results");

        // build array of decoded recipes
        Recipe[] recipes = new Recipe[0];
        for (int i = 0; i < results.length(); i++) {
            recipes = this.addRecipe(recipes, results.getJSONObject(i));
        }

        System.out.print("done\n");

        return recipes;
    }

    /**
     * Decode recipe and add it to given recipe array. 
     * todo: this is inefficient, need to find a better way
     * to do this
     */
    private Recipe[] addRecipe(Recipe[] recipes, JSONObject recipe) {
        // handles the case where recipes are nested
        // in the returned json object
        if (recipe.has("recipes")) {
            JSONArray nestedRecipes = recipe.getJSONArray("recipes");
            int nestedRecipesLen = nestedRecipes.length();

            Recipe[] newRecipes = new Recipe[nestedRecipesLen+1];
            for (int i = 0; i < nestedRecipesLen; i++) {
                newRecipes = this.addRecipe(recipes, nestedRecipes.getJSONObject(i));
            }
            return newRecipes;
        }

        // add ingredients
        JSONArray JSONIngredients = recipe.getJSONArray("sections").getJSONObject(0).getJSONArray("components");
        Ingredient[] ingredients = new Ingredient[JSONIngredients.length()];

        for (int i = 0; i < JSONIngredients.length(); i++) {
            String text = JSONIngredients.getJSONObject(i).getString("raw_text");
            ingredients[i] = new Ingredient(-1, text);
        }

        // add instructions
        JSONArray JSONInstructions = recipe.getJSONArray("instructions");
        Instruction[] instructions = new Instruction[JSONInstructions.length()];

        for (int i = 0; i < JSONInstructions.length(); i++) {
            String text = JSONInstructions.getJSONObject(i).getString("display_text");
            instructions[i] = new Instruction(-1, text, i + 1);
        }

        // copy old recipes into a new array with an empty
        // space at the end. (this is bad, need to fix)
        int recipeLen = recipes.length;
        Recipe[] newRecipes = new Recipe[recipeLen+1];
        for (int i = 0; i < recipeLen; i++) {
            newRecipes[i] = recipes[i];
        }

        // push new recipe to array
        String name = recipe.getString("name");
        newRecipes[recipeLen] = new Recipe(-1, name, ingredients, instructions);

        return newRecipes;
    }
}
