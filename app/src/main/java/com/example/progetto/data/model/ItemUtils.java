package com.example.progetto.data.model;

public class ItemUtils {
    private String name;
    private String imageUrl;

    // No-argument constructor required for Firebase deserialization
    public ItemUtils() {
        // Default constructor
    }

    // Parameterized constructor if needed
    public ItemUtils(String name, String url) {
        this.name = name;
        this.imageUrl = url;
    }

    // Getters and setters for Firebase to access fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
