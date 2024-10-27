package com.example.progetto.ui.item;

import java.util.Date;

public class Item {
    private String name;
    private int quantity;
    private Date expiryDate;

    // Costruttore
    public Item(String name, int quantity, Date expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    // Getter e Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}