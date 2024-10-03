package com.example.progetto.ui.registration;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RegistrationViewModelFactory implements ViewModelProvider.Factory {

    private final SomeRepository repository;  // esempio di un parametro

    public RegistrationViewModelFactory(SomeRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RegistrationViewModel.class)) {
            return (T) new RegistrationViewModel(repository);  // Passa il parametro al ViewModel
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
