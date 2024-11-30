package com.example.progetto.ui.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.progetto.MainViewModel;
import com.example.progetto.R;
import com.example.progetto.MainActivity;
import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.data.model.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private MainViewModel mainViewModel;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private EditText userNameEditText, userEmailEditText, userPhoneEditText, userDobEditText;
    private ImageView profileImageView;
    private Button saveChangesButton, logoutButton, resetPasswordButton, changeEmailButton, changeAvatarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        Log.d(TAG, "onCreate: ProfileActivity started");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Find views
        profileImageView = findViewById(R.id.profileImage);
        userNameEditText = findViewById(R.id.userName);
        userEmailEditText = findViewById(R.id.userEmail);
        userPhoneEditText = findViewById(R.id.userPhone);
        userDobEditText = findViewById(R.id.userDob);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        logoutButton = findViewById(R.id.logoutButton);
        changeEmailButton = findViewById(R.id.changeEmailButton);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);

        // Load current user info
        if (currentUser != null) {
            loadUserProfile();
        } else {
            showToast("No authenticated user. Please log in first.");
            Log.d(TAG, "onCreate: No authenticated user");
        }

        // Set listeners
        saveChangesButton.setOnClickListener(v -> {
            Log.d(TAG, "saveChangesButton clicked");
            updateProfile();
        });
        resetPasswordButton.setOnClickListener(v -> {
            Log.d(TAG, "resetPasswordButton clicked");
            sendPasswordResetEmail();
        });
        logoutButton.setOnClickListener(v -> {
            Log.d(TAG, "logoutButton clicked");
            mainViewModel.logout();
        });
        changeEmailButton.setOnClickListener(v -> {
            Log.d(TAG, "changeEmailButton clicked");
            navigateToChangeEmailActivity();
        });
        userDobEditText.setOnClickListener(v -> {
            Log.d(TAG, "userDobEditText clicked");
            openDatePicker();
        });
        changeAvatarButton.setOnClickListener(v -> {
            Log.d(TAG, "changeAvatarButton clicked");
            changeAvatar();
        });

        // Observe logout state
        mainViewModel.getLogoutSuccess().observe(this, success -> {
            if (success) {
                showToast("Logout successful");
                Log.d(TAG, "Logout successful");
                LoginUtils.saveGoogleLoginState(ProfileActivity.this, false);
                navigateToMainActivity();
            }
        });
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            // Dati di Firebase Authentication
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            userNameEditText.setText(displayName != null ? displayName : "No display name set");
            userEmailEditText.setText(email != null ? email : "No email set");
            userEmailEditText.setEnabled(false); // Rende il campo email non modificabile

            // Dati aggiuntivi da Firestore
            UserRepository userRepository = new UserRepository(this);
            userRepository.getUserProfile(profile -> {
                if (profile != null) {
                    userPhoneEditText.setText(profile.getPhoneNumber());
                    userDobEditText.setText(profile.getDateOfBirth());
                    Glide.with(this)
                            .load(profile.getProfileImageUrl())
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(profileImageView);
                } else {
                    Log.d(TAG, "loadUserProfile: No profile found in Firestore");
                }
            });
        } else {
            showToast("No authenticated user. Please log in first.");
            Log.e(TAG, "loadUserProfile: No authenticated user");
        }
    }

    private void updateProfile() {
        String newUserName = userNameEditText.getText().toString().trim();
        String newUserPhone = userPhoneEditText.getText().toString().trim();
        String newUserDob = userDobEditText.getText().toString().trim();

        if (!newUserName.isEmpty() && !newUserName.equals(currentUser.getDisplayName())) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUserName)
                    .build();
            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Display name updated successfully");
                            Log.d(TAG, "updateProfile: Firebase Auth profile updated successfully");
                        } else {
                            showToast("Failed to update display name");
                            Log.e(TAG, "updateProfile: Failed to update Firebase Auth profile");
                        }
                    });
        }

        // Aggiorna i campi su Firestore
        UserRepository userRepository = new UserRepository(this);
        userRepository.updateUserProfile(newUserPhone, newUserDob);
    }


    private void changeAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebaseStorage(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        String userId = currentUser.getUid();
        StorageReference fileRef = storageReference.child("profile_images/" + userId + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            UserRepository userRepository = new UserRepository(this);
                            userRepository.updateProfileImage(uri.toString());
                            Glide.with(this).load(uri).into(profileImageView);
                        }))
                .addOnFailureListener(e -> Log.e(TAG, "uploadImageToFirebaseStorage: Error uploading image", e));
    }

    private void sendPasswordResetEmail() {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Password reset email sent.");
                            }
                        });
            }
        }
    }

    private void navigateToChangeEmailActivity() {
        Intent intent = new Intent(ProfileActivity.this, ChangeEmailActivity.class);
        startActivity(intent);
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> userDobEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
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
