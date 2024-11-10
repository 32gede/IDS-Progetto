package com.example.progetto.data.model;

public class ItemUtils {
    private String id;        // New field for Firestore document ID
    private String name;
    private String imageUrl;

    // No-argument constructor required for Firebase deserialization
    public ItemUtils() {
        // Default constructor
    }

    // Parameterized constructor if needed
    public ItemUtils(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getter and setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters and setters for other fields
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
