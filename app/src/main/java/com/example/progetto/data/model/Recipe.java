package com.example.progetto.data.model;

import com.google.firebase.firestore.PropertyName;

public class Recipe {
    private String id;
    private String name;
    private String description;
    private String image;
    private String ingredients;
    private String steps;
    private String difficulty;
    private String category;
    private String preparationTime;

    // Empty constructor needed for Firebase
    public Recipe() {
    }

    public Recipe(String id, String name, String description, String image, String ingredients, String steps, String difficulty, String category, String preparationTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.ingredients = ingredients;
        this.steps = steps;
        this.difficulty = difficulty;
        this.category = category;
        this.preparationTime = preparationTime;
    }

    public Recipe(String s, String descrizioneTest, String urlDummyImmagine) {
        name = s;
        description = descrizioneTest;
        image = urlDummyImmagine;
    }

    // Getters and setters with @PropertyName to match Firestore fields

    @PropertyName("id")
    public String getId() {
        return id != null ? id : "";
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("name")
    public String getName() {
        return name != null ? name : "Titolo non disponibile";
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("description")
    public String getDescription() {
        return description != null ? description : "Descrizione non disponibile";
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("image")
    public String getImage() {
        return image != null ? image : "";
    }

    @PropertyName("image")
    public void setImage(String image) {
        this.image = image;
    }

    @PropertyName("ingredients")
    public String getIngredients() {
        return ingredients != null ? ingredients : "Ingredienti non disponibili";
    }

    @PropertyName("ingredients")
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    @PropertyName("steps")
    public String getSteps() {
        return steps != null ? steps : "Passaggi non disponibili";
    }

    @PropertyName("steps")
    public void setSteps(String steps) {
        this.steps = steps;
    }

    @PropertyName("difficulty")
    public String getDifficulty() {
        return difficulty != null ? difficulty : "Difficoltà non specificata";
    }

    @PropertyName("difficulty")
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @PropertyName("category")
    public String getCategory() {
        return category != null ? category : "Categoria non disponibile";
    }

    @PropertyName("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @PropertyName("preparationTime")
    public String getPreparationTime() {
        return preparationTime != null ? preparationTime : "Tempo di preparazione non specificato";
    }

    @PropertyName("preparationTime")
    public void setPreparationTime(String preparationTime) {
        this.preparationTime = preparationTime;
    }
}
