package com.example.progetto.data.model;

public class SelectedIngredientRecipeUtils extends SelectedIngredientUtils {
    private String recipeId;

    public SelectedIngredientRecipeUtils() {
    }
    public SelectedIngredientRecipeUtils(String name, int quantity, String recipeId, int position) {
        super(name, quantity);
        this.recipeId = recipeId;
        this.setPosition(position);
    }
    public String getRecipeId() {
        return recipeId;
    }
    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}
