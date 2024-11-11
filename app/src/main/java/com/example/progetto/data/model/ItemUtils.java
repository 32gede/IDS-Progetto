package com.example.progetto.data.model;

public class ItemUtils {
    private String productId;        // New field for Firestore document ID
    private String name;
    private String imageUrl;

    // No-argument constructor required for Firebase deserialization
    public ItemUtils() {
        // Default constructor
    }

    // Parameterized constructor if needed
    public ItemUtils(String productId, String name, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getter and setter for id
    public String getProductId() {
        return productId;
    }
    public void setId(String productId) {
        this.productId = productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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
