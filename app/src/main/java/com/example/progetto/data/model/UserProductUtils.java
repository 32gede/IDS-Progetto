package com.example.progetto.data.model;

public class UserProductUtils {
    private String productId;
    private String userId;
    private String title;
    private String expiryDate;
    private int quantity;

    // Constructor
    public UserProductUtils() {
    }

    public UserProductUtils(String productId, String title, String expiryDate, int quantity, String userId) {
        this.productId = productId;
        this.title = title;
        this.expiryDate = expiryDate;
        this.userId = userId;
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
