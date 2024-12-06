package com.example.progetto.data.model;

import java.util.Objects;

public class SelectedIngredientUtils {
    private String name;
    private int quantity;
    private int position;

    public SelectedIngredientUtils() {
    }

    public SelectedIngredientUtils(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        position = -1;
    }

    @Override
    public String toString() {
        return "SelectedIngredientUtils{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectedIngredientUtils that = (SelectedIngredientUtils) o;
        return quantity == that.quantity && position == that.position && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, position);
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

