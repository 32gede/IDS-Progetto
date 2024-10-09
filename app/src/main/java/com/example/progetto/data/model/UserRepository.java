package com.example.progetto.data.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;

public class UserRepository {
    private final FirebaseAuth mAuth;
    private final SharedPreferences sharedPreferences;

    public UserRepository(Context context) {
        this.mAuth = FirebaseAuth.getInstance();
        this.sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    public Task<AuthResult> registerUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginUser(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserToPreferences(user);
                }
            }
        });
    }

    public void logout() {
        mAuth.signOut();
        sharedPreferences.edit().clear().apply();
    }

    public FirebaseUser getLoggedInUser() {
        String userId = sharedPreferences.getString("userId", null);
        if (userId != null) {
            return mAuth.getCurrentUser();
        }
        return null;
    }

    private void saveUserToPreferences(FirebaseUser user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", user.getUid());
        editor.putString("email", user.getEmail());
        editor.apply();
    }
}