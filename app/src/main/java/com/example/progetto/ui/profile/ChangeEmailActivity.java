package com.example.progetto.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.progetto.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;

public class ChangeEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText newEmailEditText, passwordEditText;
    private Button updateEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Find views
        newEmailEditText = findViewById(R.id.newEmail);
        passwordEditText = findViewById(R.id.password);
        updateEmailButton = findViewById(R.id.updateEmailButton);

        // Set click listener
        updateEmailButton.setOnClickListener(v -> updateEmail());
    }

    private void updateEmail() {
        String newEmail = newEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail) || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            showToast("Invalid email format");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showToast("Please enter your password to re-authenticate.");
            return;
        }

        if (currentUser == null) {
            showToast("No authenticated user. Please log in again.");
            return;
        }

        // Re-authenticate the user
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        // Send verification email to the new address
                        sendVerificationEmail(newEmail);
                    } else {
                        showToast("Re-authentication failed. Incorrect password.");
                    }
                });
    }

    private void sendVerificationEmail(String newEmail) {
        currentUser.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Verification email sent to " + newEmail + ". Please verify to complete the update.");
                    } else {
                        showToast("Failed to send verification email.");
                        Log.e("ChangeEmail", "Error sending verification email: " + task.getException());
                    }
                });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}