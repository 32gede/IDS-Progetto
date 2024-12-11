package com.example.progetto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.data.model.LoginUtils;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.login.LoginActivity;
import com.example.progetto.ui.registration.RegistrationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button loginButton, registerButton;

    private final ActivityResultLauncher<Intent> authLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    updateUI();
                } else {
                    Log.w(TAG, "Autenticazione fallita o annullata");
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Controlla se l'utente è loggato
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Utente autenticato, reindirizzamento alla HomeActivity.");
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Configura i pulsanti di login e registrazione
        loginButton = findViewById(R.id.Login);
        registerButton = findViewById(R.id.Registration);

        loginButton.setOnClickListener(v -> {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            authLauncher.launch(loginIntent);
        });

        registerButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(MainActivity.this, RegistrationActivity.class);
            authLauncher.launch(registerIntent);
        });
    }

    private void updateUI() {
        boolean loggedIn = LoginUtils.isLoggedIn(this);
        Log.d(TAG, "Stato login: " + loggedIn);
        loginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        registerButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
    }
}
