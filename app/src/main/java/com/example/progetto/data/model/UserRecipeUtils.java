package com.example.progetto.data.model;

public class UserRecipeUtils extends Recipe {
    private String userId; // User-specific field

    // No-argument constructor required for Firestore
    public UserRecipeUtils() {}

    // Constructor that accepts a Recipe object and a userId
    public UserRecipeUtils(Recipe recipe, String userId) {
        super(recipe.getId(), recipe.getName(), recipe.getDescription(), recipe.getImage(),
                recipe.getIngredients(), recipe.getSteps(), recipe.getDifficulty(),
                recipe.getCategory(), recipe.getPreparationTime(), recipe.getAverageRating());
        this.userId = userId;
    }

    // Full constructor with all fields
    public UserRecipeUtils(String id, String name, String description, String image, String ingredients,
                           String steps, String difficulty, String category, String preparationTime, String userId) {
        super(id, name, description, image, ingredients, steps, difficulty, category, preparationTime,5);
        this.userId = userId;
    }

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
