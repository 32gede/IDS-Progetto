package com.example.progetto.data.model;

public class SelectedIngredientUtils {
    private String name;
    private int quantity;
    private int position;

    public SelectedIngredientUtils(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        position = -1;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

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

}

