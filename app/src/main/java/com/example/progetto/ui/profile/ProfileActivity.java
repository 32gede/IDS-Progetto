package com.example.progetto.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.progetto.MainViewModel;
import com.example.progetto.R;
import com.example.progetto.MainActivity;
import com.example.progetto.data.model.LoginUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText userNameEditText, userEmailEditText, userPasswordEditText;
    private Button saveChangesButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Find views
        userNameEditText = findViewById(R.id.userName);
        userEmailEditText = findViewById(R.id.userEmail);
        userPasswordEditText = findViewById(R.id.userPassword);
        logoutButton = findViewById(R.id.logoutButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Set current user info
        if (currentUser != null) {
            userNameEditText.setText(currentUser.getDisplayName());
            userEmailEditText.setText(currentUser.getEmail());
        }

        // Set click listeners
        saveChangesButton.setOnClickListener(v -> updateProfile());

        logoutButton.setOnClickListener(v -> mainViewModel.logout());

        // Observe logout state
        mainViewModel.getLogoutSuccess().observe(this, success -> {
            if (success) {
                showToast("Logout successful");
                LoginUtils.saveGoogleLoginState(ProfileActivity.this, false);
                navigateToMainActivity();
            }
        });
    }

    private void updateProfile() {
        String newUserName = userNameEditText.getText().toString();
        String newUserEmail = userEmailEditText.getText().toString();
        String newUserPassword = userPasswordEditText.getText().toString();

        if (!newUserName.isEmpty()) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUserName)
                    .build();
            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Username updated");
                        }
                    });
        }

        if (!newUserEmail.isEmpty()) {
            currentUser.updateEmail(newUserEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Email updated");
                        }
                    });
        }

        if (!newUserPassword.isEmpty()) {
            if (newUserPassword.length() >= 6) {
                currentUser.updatePassword(newUserPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Password updated");
                            }
                        });
            } else {
                showToast("Password must be at least 6 characters");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}