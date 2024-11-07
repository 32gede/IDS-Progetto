package com.example.progetto.ui.profile;

import android.content.Intent;  // Aggiungi l'import per Intent
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.progetto.MainViewModel;
import com.example.progetto.R;
import com.example.progetto.MainActivity;
import com.example.progetto.data.model.LoginUtils;
public class ProfileActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Inizializza il ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Trova il bottone di logout
        Button logoutButton = findViewById(R.id.logoutButton);

        // Imposta il click listener per il bottone di logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Esegui il logout
                mainViewModel.logout();  // Questo metodo gestisce il logout nel ViewModel
            }
        });

        // Osserva lo stato del logout
        mainViewModel.getLogoutSuccess().observe(this, success -> {
            if (success) {
                showToast("Logout avvenuto con successo");
                // Salva lo stato di login
                LoginUtils.saveGoogleLoginState(ProfileActivity.this, false);
                // Torna alla MainActivity
                navigateToMainActivity();
            }
        });
    }

    // Metodo di utilità per mostrare un messaggio Toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Metodo per navigare alla MainActivity
    private void navigateToMainActivity() {
        // Crea un Intent per avviare ActivityMain
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);  // Avvia ActivityMain
        finish();  // Chiudi ProfileActivity
    }
}
