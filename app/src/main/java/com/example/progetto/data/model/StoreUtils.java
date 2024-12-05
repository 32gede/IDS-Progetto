package com.example.progetto.data.model;

import java.io.Serializable;

public class StoreUtils implements Serializable{
    private String id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private String products;

    public StoreUtils() {
    }

    public StoreUtils(String id, String userId, String products, String description, String name, Double prezzo, String image) {
        this.id = id;
        this.userId = userId;
        this.products = products;
        this.description = description;
        this.name = name;
        this.price = prezzo;
        this.image = image;
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

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String userId;
}
