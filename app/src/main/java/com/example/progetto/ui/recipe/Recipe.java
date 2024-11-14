package com.example.progetto.ui.recipe;

import com.google.firebase.firestore.PropertyName;

public class Recipe {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String ingredients;
    private String steps;
    private String difficulty;
    private String category;
    private String preparationTime;

    // Empty constructor needed for Firebase
    public Recipe() {}

    public Recipe(String id, String title, String description, String imageUrl, String ingredients, String steps, String difficulty, String category, String preparationTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.steps = steps;
        this.difficulty = difficulty;
        this.category = category;
        this.preparationTime = preparationTime;
    }

    // Getters and Setters
    @PropertyName("id")
    public String getId() { return id; }
    @PropertyName("id")
    public void setId(String id) { this.id = id; }

    @PropertyName("title")
    public String getTitle() { return title; }
    @PropertyName("title")
    public void setTitle(String title) { this.title = title; }

    @PropertyName("description")
    public String getDescription() { return description; }
    @PropertyName("description")
    public void setDescription(String description) { this.description = description; }

    @PropertyName("imageUrl")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("ingredients")
    public String getIngredients() { return ingredients; }
    @PropertyName("ingredients")
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    @PropertyName("steps")
    public String getSteps() { return steps; }
    @PropertyName("steps")
    public void setSteps(String steps) { this.steps = steps; }

    @PropertyName("difficulty")
    public String getDifficulty() { return difficulty; }
    @PropertyName("difficulty")
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    @PropertyName("category")
    public String getCategory() { return category; }
    @PropertyName("category")
    public void setCategory(String category) { this.category = category; }

    @PropertyName("preparationTime")
    public String getPreparationTime() { return preparationTime; }
    @PropertyName("preparationTime")
    public void setPreparationTime(String preparationTime) { this.preparationTime = preparationTime; }
}