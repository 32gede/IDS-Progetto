package com.example.progetto.data.model;

import java.util.Map;

public class UserProfile {
    private String username;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String profileImageUrl;

    // Costruttore vuoto richiesto da Firestore
    public UserProfile() {
    }

    // Costruttore completo
    public UserProfile(String username, String email, String phoneNumber, String dateOfBirth) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    // Getter e Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "username", username,
                "email", email,
                "phoneNumber", phoneNumber,
                "dateOfBirth", dateOfBirth,
                "profileImageUrl", profileImageUrl
        );
    }
}
