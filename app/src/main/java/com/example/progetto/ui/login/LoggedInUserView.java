package com.example.progetto.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
public class LoggedInUserView {
    private final String displayName;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}