package com.example.progetto.data.model;

import java.util.Map;

public class UserProfile {
    private String uid;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String profileImageUrl;

    // Costruttore vuoto richiesto da Firestore
    public UserProfile() {
    }

    // Costruttore completo
    public UserProfile(String uid, String email, String phoneNumber, String dateOfBirth) {
        this.uid = uid;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    // Getter e Setter
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
                "uid", uid,
                "email", email,
                "phoneNumber", phoneNumber,
                "dateOfBirth", dateOfBirth,
                "profileImageUrl", profileImageUrl
        );
    }
}
