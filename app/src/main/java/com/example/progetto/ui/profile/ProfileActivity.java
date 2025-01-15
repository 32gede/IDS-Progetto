package com.example.progetto.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.progetto.MainActivity;
import com.example.progetto.MainViewModel;
import com.example.progetto.R;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MainViewModel mainViewModel;

    private TextView  userEmailTextView, userPhoneTextView, userDobTextView;
    private ImageView profileImageView;
    private Button editProfileButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        profileImageView = findViewById(R.id.profileImage);
        userEmailTextView = findViewById(R.id.userEmail);
        userPhoneTextView = findViewById(R.id.userPhone);
        userDobTextView = findViewById(R.id.userDob);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);

        if (currentUser != null) {
            loadUserProfile();
        } else {
            showToast("No authenticated user. Please log in first.");
        }

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            mainViewModel.logout();
        });

        // Observe logout state
        mainViewModel.getLogoutSuccess().observe(this, success -> {
            if (success) {
                showToast("Logout successful");
                LoginUtils.saveGoogleLoginState(ProfileActivity.this, false);
                navigateToMainActivity();
            }
        });
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            String email = currentUser.getEmail();

            userEmailTextView.setText(email != null ? email : "No email set");

            UserRepository userRepository = new UserRepository(this);
            userRepository.getUserProfile(profile -> {
                if (profile != null) {
                    userPhoneTextView.setText(profile.getPhoneNumber());
                    userDobTextView.setText(profile.getDateOfBirth());
                    Glide.with(this)
                            .load(profile.getProfileImageUrl())
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(profileImageView);
                }
            });
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

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // Ricarica i dati ogni volta che la schermata diventa visibile
    }
}
