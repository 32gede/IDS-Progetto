package com.example.progetto.data.model;

public class ItemUtils {
    private String name;
    private String url;

    // No-argument constructor required for Firebase deserialization
    public ItemUtils() {
        // Default constructor
    }

    // Parameterized constructor if needed
    public ItemUtils(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // Getters and setters for Firebase to access fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
