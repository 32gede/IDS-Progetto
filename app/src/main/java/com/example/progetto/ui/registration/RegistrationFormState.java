package com.example.progetto.ui.registration;

public class RegistrationFormState {
    private Integer usernameError;
    private Integer passwordError;
    private boolean isDataValid;

    RegistrationFormState(Integer usernameError, Integer passwordError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    RegistrationFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    public Integer getUsernameError() {
        return usernameError;
    }

    public Integer getPasswordError() {
        return passwordError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}
