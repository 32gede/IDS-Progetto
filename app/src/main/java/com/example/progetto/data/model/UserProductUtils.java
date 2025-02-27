package com.example.progetto.data.model;

import java.util.Map;

public class UserProductUtils  {
    private String userId;
    private String expiryDate;
    private int quantity;
    private String name,url;
    private String id;

    // Constructor
    public UserProductUtils() {
        super();
    }

    public UserProductUtils(String productId, String name, String expiryDate, int quantity, String userId, String url) {
        this.name = name;
        this.url = url;
        this.expiryDate = expiryDate;
        this.userId = userId;
        this.quantity = quantity;
        this.id = productId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "expiryDate", expiryDate,
                "quantity", quantity,
                "userId", userId,
                "url", url
        );
    }

}
