package com.example.progetto.data.model;

import java.util.Map;

public class UserRecipeUtils {
    private String userId; // User-specific field
    private String documentId; // Firestore document ID

    // No-argument constructor required for Firestore
    public UserRecipeUtils() {}

    // Constructor that accepts a Recipe object and a userId
    public UserRecipeUtils(String userId,String documentId){
        this.userId = userId;
        this.documentId = documentId;
    }

    // Getter and Setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public Map<String, Object> toMap() {
        return Map.of(
                "userId", userId,
                "documentId", documentId
        );
    }
}
