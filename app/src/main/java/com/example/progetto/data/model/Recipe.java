package com.example.progetto.data.model;

import android.util.Log;

import java.io.Serializable;
import java.util.Map;

public class Recipe implements Serializable {
    private String id; // Unique identifier for the recipe
    private String name;
    private String description;
    private String image;
    private String steps;
    private String difficulty;
    private String category;
    private String preparationTime;
    private float averageRating;

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    // No-argument constructor required for Firestore
    public Recipe() {
    }

    public Recipe(String id, String name, String description, String image, String difficulty, String steps, String category, String preparationTime, float rating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.difficulty = difficulty;
        this.steps = steps;
        this.category = category;
        this.preparationTime = preparationTime;
        this.averageRating = rating;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(String preparationTime) {
        this.preparationTime = preparationTime;
    }
    public Map<String, Object> toMap() {
        Log.d("Recipe", "toMap called with id: " + id);
        Log.d("Recipe", "toMap called with name: " + name);
        Log.d("Recipe", "toMap called with description: " + description);
        Log.d("Recipe", "toMap called with image: " + image);
        Log.d("Recipe", "toMap called with steps: " + steps);
        Log.d("Recipe", "toMap called with difficulty: " + difficulty);
        Log.d("Recipe", "toMap called with category: " + category);
        Log.d("Recipe", "toMap called with preparationTime: " + preparationTime);
        Log.d("Recipe", "toMap called with averageRating: " + averageRating);


        return Map.of(
                "id", id,
                "name", name,
                "description", description,
                "image", image != null ? image : "",
                "steps", steps,
                "difficulty", difficulty,
                "category", category,
                "preparationTime", preparationTime,
                "averageRating", averageRating
        );
    }
}
