package com.example.progetto.ui.registration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.R;

public class RegistrationViewModel extends ViewModel {

    private MutableLiveData<RegistrationFormState> registrationFormState = new MutableLiveData<>();
    private MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();

    // Getter per lo stato del form di registrazione
    public LiveData<RegistrationFormState> getRegistrationFormState() {
        return registrationFormState;
    }

    // Getter per il risultato della registrazione
    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }

    // Funzione per gestire il cambiamento dei dati nel form
    public void registrationDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            registrationFormState.setValue(new RegistrationFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            registrationFormState.setValue(new RegistrationFormState(null, R.string.invalid_password));
        } else {
            registrationFormState.setValue(new RegistrationFormState(true));
        }
    }

    // Funzione per effettuare la registrazione (da definire con la tua logica)
    public void register(String username, String password) {
        // Aggiungi qui la logica di registrazione (come la chiamata a un server)
        registrationResult.setValue(new RegistrationResult(new RegisteredUserView(username)));
    }

    // Validazione del nome utente
    private boolean isUserNameValid(String username) {
        return username != null && username.trim().length() > 3;
    }

    // Validazione della password
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}