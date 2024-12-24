package com.example.progetto.data.model;

public class NotificationItem {
    private String productName;
    private String userId;
    private int quantity;
    private String expiryDate;
    private String id;

    public NotificationItem() {
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NotificationItem(String id,String title, String message, String timestamp) {
        this.productName = title;
        this.userId = message;
        this.expiryDate = timestamp;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public String getUserId() {
        return userId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}
