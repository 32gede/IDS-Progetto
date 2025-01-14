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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserRepository {
    private final FirebaseAuth mAuth;
    private final SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    public UserRepository(Context context) {
        this.mAuth = FirebaseAuth.getInstance();
        this.sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set the locale for Firebase Auth
        String locale = Locale.getDefault().toString();
        if (locale != null && !locale.isEmpty()) {
            mAuth.setLanguageCode(locale);
        } else {
            mAuth.setLanguageCode("en"); // Default to English if locale is null or empty
        }
    }

    public Task<AuthResult> registerUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserToPreferences(user);
                    saveUserToFirestore(user);
                }
            }
        });
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
        return mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToPreferences(user);
                            saveUserToFirestore(user);
                            Log.d("UserRepository", "Login con Google riuscito per: " + user.getEmail());
                        }
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("UserRepository", "Errore nel login con Google: " + e.getMessage());
                        } else {
                            Log.e("UserRepository", "Errore nel login con Google: task non completato con successo");
                        }
                    }
                });
    }


    private void saveUserToFirestore(FirebaseUser user) {
        // Creazione di un oggetto UserProfile
        UserProfile userProfile = new UserProfile(
                user.getUid(),
                user.getEmail(),
                "", // Phone number vuoto per ora
                ""  // Date of birth vuoto per ora
        );

        // Salvataggio nel documento Firestore
        db.collection("users").document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> Log.d("UserRepository", "User profile added to Firestore"))
                .addOnFailureListener(e -> Log.w("UserRepository", "Error adding user profile to Firestore", e));
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

    public void updateUserProfile(String Username, String phoneNumber, String dateOfBirth) {
        FirebaseUser user = mAuth.getCurrentUser(); // Ottieni l'utente attualmente loggato

        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("username", Username);
            updates.put("phoneNumber", phoneNumber);
            updates.put("dateOfBirth", dateOfBirth);

            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> Log.d("UserRepository", "User profile updated successfully"))
                    .addOnFailureListener(e -> Log.e("UserRepository", "Error updating user profile", e));
        } else {
            Log.e("UserRepository", "No authenticated user to update");
        }
    }

    public interface UserProfileCallback {
        void onUserProfileLoaded(UserProfile userProfile);
    }

    public void getUserProfile(UserProfileCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                            callback.onUserProfileLoaded(userProfile);
                        } else {
                            Log.d("UserRepository", "getUserProfile: No profile found");
                            callback.onUserProfileLoaded(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserRepository", "getUserProfile: Error fetching user profile", e);
                        callback.onUserProfileLoaded(null);
                    });
        } else {
            Log.e("UserRepository", "getUserProfile: No authenticated user");
            callback.onUserProfileLoaded(null);
        }
    }

    public void updateProfileImage(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("profileImageUrl", imageUrl);

            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> Log.d("UserRepository", "updateProfileImage: Profile image updated"))
                    .addOnFailureListener(e -> Log.e("UserRepository", "updateProfileImage: Error updating profile image", e));
        } else {
            Log.e("UserRepository", "updateProfileImage: No authenticated user");
        }
    }


}