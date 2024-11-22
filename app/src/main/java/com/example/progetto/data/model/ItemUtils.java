package com.example.progetto.data.model;

import androidx.annotation.Nullable;

public class ItemUtils {
    private String productId; // ID del prodotto nel database
    private String name;      // Nome del prodotto
    @Nullable
    private String category;  // Categoria del prodotto, opzionale
    private String imageUrl;  // URL dell'immagine del prodotto
    @Nullable
    private long timestamp;   // Timestamp di creazione

    // Costruttore vuoto richiesto da Firestore
    public ItemUtils() {}

    // Costruttore parametrizzato
    public ItemUtils(String productId, String name, @Nullable String category, String imageUrl, @Nullable long timestamp) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getter e Setter
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Nullable
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@Nullable long timestamp) {
        this.timestamp = timestamp;
    }
}
