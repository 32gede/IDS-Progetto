package com.example.progetto.ui.registration;

public class RegistrationResult {
    private RegisteredUserView success;
    private Integer error;

    RegistrationResult(RegisteredUserView success) {
        this.success = success;
        this.error = null;
    }

    RegistrationResult(Integer error) {
        this.error = error;
        this.success = null;
    }

    public RegisteredUserView getSuccess() {
        return success;
    }

    public Integer getError() {
        return error;
    }
}