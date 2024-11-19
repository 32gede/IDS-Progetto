package com.example.progetto.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
    private Button saveChangesButton, logoutButton, resetPasswordButton;

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
        saveChangesButton = findViewById(R.id.saveChangesButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Set current user info
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (displayName != null) {
                userNameEditText.setText(displayName);
            } else {
                userNameEditText.setText("No display name set");
            }

            if (email != null) {
                userEmailEditText.setText(email);
            } else {
                userEmailEditText.setText("No email set");
            }
        } else {
            showToast("No authenticated user. Please log in first.");
        }

        // Set click listeners
        saveChangesButton.setOnClickListener(v -> updateProfile());
        resetPasswordButton.setOnClickListener(v -> sendPasswordResetEmail());
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
        String newUserName = userNameEditText.getText().toString().trim();
        String newUserEmail = userEmailEditText.getText().toString().trim();
        boolean isValid = true;

        // Validate new email
        if (!TextUtils.isEmpty(newUserEmail) && !Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {
            showToast("Invalid email format");
            isValid = false;
        }

        if (!TextUtils.isEmpty(newUserName)) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUserName)
                    .build();
            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Username updated");
                        } else {
                            showToast("Failed to update username");
                        }
                    });
        }

        if (isValid && !TextUtils.isEmpty(newUserEmail)) {
            currentUser.updateEmail(newUserEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Email updated. Please check your previous email for confirmation.");
                        } else {
                            showToast("Failed to update email. Please re-authenticate and try again.");
                        }
                    });
        }
    }

    private void sendPasswordResetEmail() {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Password reset email sent. Please check your email.");
                            } else {
                                showToast("Failed to send password reset email.");
                            }
                        });
            } else {
                showToast("Invalid email address. Cannot send reset email.");
            }
        } else {
            showToast("No authenticated user. Please log in first.");
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
