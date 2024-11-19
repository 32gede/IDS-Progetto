package com.example.progetto.data.model;

public class RecipeUser extends Recipe {
    private String userId;

    public RecipeUser() {
    }

    public RecipeUser(String name, String description, String ingredients, String steps, String difficulty, String category, String preparationTime, String image, String userId) {
        super(name, description, ingredients, steps, difficulty, category, preparationTime, image);
        this.userId = userId;
    }
    public void carica(Recipe recipe){
        this.setName(recipe.getName());
        this.setDescription(recipe.getDescription());
        this.setIngredients(recipe.getIngredients());
        this.setSteps(recipe.getSteps());
        this.setDifficulty(recipe.getDifficulty());
        this.setCategory(recipe.getCategory());
        this.setPreparationTime(recipe.getPreparationTime());
        this.setImage(recipe.getImage());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
