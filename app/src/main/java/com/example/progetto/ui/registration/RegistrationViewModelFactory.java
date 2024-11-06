package com.example.progetto.ui.registration;

import android.content.Context;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.example.progetto.data.model.UserRepository;

/**
 * ViewModel provider factory to instantiate RegistrationViewModel.
 * Required given RegistrationViewModel has a non-empty constructor
 */
public class RegistrationViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository repository;
    private final Context context;

    public RegistrationViewModelFactory(UserRepository repository, Context context) {
        this.repository = repository;
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RegistrationViewModel.class)) {
            return (T) new RegistrationViewModel();
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}