package com.example.progetto.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.UserRepository;
import com.example.progetto.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private TextView userNameTextView, userEmailTextView, userPhoneTextView, userDobTextView;
    private ImageView profileImageView;
    private Button logoutButton, editProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Inizializzazione Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Trova le viste
        profileImageView = findViewById(R.id.profileImage);
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        userPhoneTextView = findViewById(R.id.userPhone);
        userDobTextView = findViewById(R.id.userDob);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);

        // Listener per il pulsante di modifica del profilo
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);

            // Passa i dati attuali all'activity di modifica
            intent.putExtra("userName", userNameTextView.getText().toString());
            intent.putExtra("userPhone", userPhoneTextView.getText().toString());
            intent.putExtra("userDob", userDobTextView.getText().toString());

            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "logoutButton: clicked");

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Assicurati di terminare l'attuale attività
        });


        // Carica le informazioni dell'utente
        if (currentUser != null) {
            loadUserProfile();
        } else {
            Toast.makeText(this, "No authenticated user. Please log in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            userNameTextView.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "No name provided");
            userEmailTextView.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email provided");
        }

        // Carica altre informazioni dal repository
        UserRepository userRepository = new UserRepository(this);
        userRepository.getUserProfile(profile -> {
            if (profile != null) {
                userPhoneTextView.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "No phone number provided");
                userDobTextView.setText(profile.getDateOfBirth() != null ? profile.getDateOfBirth() : "No date of birth provided");
                Glide.with(this)
                        .load(profile.getProfileImageUrl())
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .into(profileImageView);
            } else {
                Log.d(TAG, "No profile data found.");
            }
        });
    }
}
