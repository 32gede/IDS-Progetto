package com.example.progetto.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.UserProfile;
import com.example.progetto.data.model.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private EditText userNameEditText, userPhoneEditText, userDobEditText;
    private Button saveChangesButton, cancelButton, changeEmailButton, changeAvatarButton;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(this);

        profileImageView = findViewById(R.id.profileImageView);
        userNameEditText = findViewById(R.id.editUserName);
        userPhoneEditText = findViewById(R.id.editUserPhone);
        userDobEditText = findViewById(R.id.editUserDob);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        cancelButton = findViewById(R.id.cancelButton);
        changeEmailButton = findViewById(R.id.changeEmailButton);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);

        loadUserProfile();

        saveChangesButton.setOnClickListener(v -> updateUserProfile());
        cancelButton.setOnClickListener(v -> finish());
        userDobEditText.setOnClickListener(v -> openDatePicker());
        changeEmailButton.setOnClickListener(v -> navigateToChangeEmailActivity());
        changeAvatarButton.setOnClickListener(v -> selectNewProfilePicture());
    }

    private void loadUserProfile() {
        userRepository.getUserProfile(profile -> {
            if (profile != null) {
                userNameEditText.setText(profile.getUid());
                userPhoneEditText.setText(profile.getPhoneNumber());
                userDobEditText.setText(profile.getDateOfBirth());

                Glide.with(this)
                        .load(profile.getProfileImageUrl())
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .into(profileImageView);
            } else {
                Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile() {
        String newUserName = userNameEditText.getText().toString().trim();
        String newUserPhone = userPhoneEditText.getText().toString().trim();
        String newUserDob = userDobEditText.getText().toString().trim();

        userRepository.updateUserProfile(newUserPhone, newUserDob);
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void openDatePicker() {
        // Implement date picker logic here
    }

    private void navigateToChangeEmailActivity() {
        Intent intent = new Intent(EditProfileActivity.this, ChangeEmailActivity.class);
        startActivity(intent);
    }

    private void selectNewProfilePicture() {
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
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Implement Firebase upload logic here
        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
    }
}
