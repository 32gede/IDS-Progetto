package com.example.progetto.ui.registration;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.progetto.data.model.UserRepository;

public class RegistrationViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository repository;  // esempio di un parametro

    public RegistrationViewModelFactory(UserRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RegistrationViewModel.class)) {
            return (T) new RegistrationViewModel();  // Passa il parametro al ViewModel
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
