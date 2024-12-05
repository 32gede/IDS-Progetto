package com.example.progetto.data.model;

public class SelectedIngredientStoreUtils extends SelectedIngredientUtils {
    private String recipeId;

    public SelectedIngredientStoreUtils(String name, int quantity, String recipeId) {
        super(name, quantity);
        this.recipeId = recipeId;
        this.setPosition(2);
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}
