package com.example.progetto.data.model;

public class Recipe {
    private String id;
    private String name;
    private String description;
    private String image;

    // No-argument constructor required for Firestore
    public Recipe() {
    }

    public Recipe(String id, String name, String description, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}