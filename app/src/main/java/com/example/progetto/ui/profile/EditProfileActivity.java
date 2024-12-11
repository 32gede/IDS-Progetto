package com.example.progetto.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.example.progetto.data.model.UserProfile;
import com.example.progetto.data.model.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private EditText userNameEditText, userPhoneEditText, userDobEditText;
    private Button saveChangesButton, cancelButton;
    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(this);

        userNameEditText = findViewById(R.id.editUserName);
        userPhoneEditText = findViewById(R.id.editUserPhone);
        userDobEditText = findViewById(R.id.editUserDob);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        cancelButton = findViewById(R.id.cancelButton);

        loadUserProfile();

        saveChangesButton.setOnClickListener(v -> updateUserProfile());
        cancelButton.setOnClickListener(v -> finish());

        userDobEditText.setOnClickListener(v -> openDatePicker());
    }

    private void loadUserProfile() {
        userRepository.getUserProfile(profile -> {
            if (profile != null) {
                userNameEditText.setText(profile.getUid());
                userPhoneEditText.setText(profile.getPhoneNumber());
                userDobEditText.setText(profile.getDateOfBirth());
            }
        });
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

    private void updateUserProfile() {
        String newUserName = userNameEditText.getText().toString().trim();
        String newUserPhone = userPhoneEditText.getText().toString().trim();
        String newUserDob = userDobEditText.getText().toString().trim();

        userRepository.updateUserProfile(newUserPhone, newUserDob);

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
