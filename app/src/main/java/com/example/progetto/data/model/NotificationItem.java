package com.example.progetto.data.model;

public class NotificationItem {
    private String Name;
    private String userId;
    private int quantity;
    private String expiryDate;

    public NotificationItem() {
    }

    public NotificationItem(String title, String message, String timestamp) {
        this.Name = title;
        this.userId = message;
        this.expiryDate = timestamp;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return Name;
    }

    public String getUserId() {
        return userId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}
