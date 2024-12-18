package com.example.progetto.data.model;

import java.util.Map;

public class UserRecipeUtils {
    private String userId; // User-specific field
    private String id; // Firestore document ID

    // No-argument constructor required for Firestore
    public UserRecipeUtils() {}

    // Constructor that accepts a Recipe object and a userId
    public UserRecipeUtils(String userId,String documentId){
        this.userId = userId;
        this.id = documentId;
    }

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Map<String, Object> toMap() {
        return Map.of(
                "userId", userId,
                "documentId", id
        );
    }
}
