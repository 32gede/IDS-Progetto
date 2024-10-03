package com.example.progetto.data.model;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import androidx.lifecycle.MutableLiveData;

public class UserRepository {
    private FirebaseAuth mAuth;

    public UserRepository() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public MutableLiveData<FirebaseUser> registerUser(String email, String password) {
        MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                        Log.e("UserRepository", "Registration successful", task.getException());
                    } else {
                        userLiveData.setValue(null);
                        // Log the error or handle it as needed
                        Log.e("UserRepository", "Registration failed", task.getException());
                    }
                });

        return userLiveData;
    }

    public MutableLiveData<FirebaseUser> loginUser(String email, String password) {
        MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                        userLiveData.setValue(null);
                        // Log the error or handle it as needed
                        Log.e("UserRepository", "Login failed", task.getException());
                    }
                });

        return userLiveData;
    }
}