package com.example.progetto.ui.registration;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.R;
import com.example.progetto.data.model.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationViewModel extends ViewModel {

    private final UserRepository userRepository;
    private MutableLiveData<RegistrationFormState> registrationFormState = new MutableLiveData<>();
    private MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();

    // Constructor that accepts a UserRepository
    public RegistrationViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        userRepository.registerUser(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    registrationResult.setValue(new RegistrationResult(new RegisteredUserView(username)));
                } else {
                    Log.e("RegistrationViewModel", "Registration failed", task.getException());
                    registrationResult.setValue(new RegistrationResult(R.string.registration_failed));
                }
            }
        });
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