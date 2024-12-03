package com.example.progetto.data.model;

public class UserProductUtils  {
    private String userId;
    private String expiryDate;
    private int quantity;
    private String name,url;
    private String productId;

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
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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
}
