package com.example.progetto.data.model;

public class Recipe {
    private String name;
    private String description;
    private String image;
    private String ingredients;
    private String steps;
    private String difficulty;
    private String category;
    private String preparationTime;

    // No-argument constructor required for Firestore
    public Recipe() {
    }

    public Recipe( String name, String description, String image, String ingredients, String steps, String difficulty, String category, String preparationTime) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.ingredients = ingredients;
        this.steps = steps;
        this.difficulty = difficulty;
        this.category = category;
        this.preparationTime = preparationTime;
    }


    public String getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(String preparationTime) {
        this.preparationTime = preparationTime;
    }


    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}