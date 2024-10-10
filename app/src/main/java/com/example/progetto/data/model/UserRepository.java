package com.example.progetto.data.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class UserRepository {
    private final FirebaseAuth mAuth;
    private final SharedPreferences sharedPreferences;

    public UserRepository(Context context) {
        this.mAuth = FirebaseAuth.getInstance();
        this.sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Set the locale for Firebase Auth
        String locale = Locale.getDefault().toString();
        if (locale != null && !locale.isEmpty()) {
            mAuth.setLanguageCode(locale);
        } else {
            mAuth.setLanguageCode("en"); // Default to English if locale is null or empty
        }
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

    public Task<AuthResult> loginWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
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