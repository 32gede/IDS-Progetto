package com.example.progetto.ui.registration;

import android.content.Context;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.R;
import com.example.progetto.data.model.UserRepository;

public class RegistrationViewModel extends ViewModel {

    private MutableLiveData<RegistrationFormState> registrationFormState = new MutableLiveData<>();
    private MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();
    private UserRepository userRepository;
    private Context context;

    RegistrationViewModel(UserRepository userRepository, Context context) {
        this.userRepository = userRepository;
        this.context = context;
    }

    public LiveData<RegistrationFormState> getRegistrationFormState() {
        return registrationFormState;
    }

    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }

    public void register(String username, String password) {
        userRepository.registerUser(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                registrationResult.setValue(new RegistrationResult(new RegisteredUserView(username)));
            } else {
                registrationResult.setValue(new RegistrationResult(R.string.registration_failed));
            }
        });
    }

    public void registrationDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            registrationFormState.setValue(new RegistrationFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            registrationFormState.setValue(new RegistrationFormState(null, R.string.invalid_password));
        } else {
            registrationFormState.setValue(new RegistrationFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        return username != null && username.contains("@") && Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}