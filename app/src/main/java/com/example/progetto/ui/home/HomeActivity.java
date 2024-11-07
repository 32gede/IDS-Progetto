package com.example.progetto.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.R;

public class HomeActivity extends AppCompatActivity {

    private View homeBackgroundCircle;
    private ImageButton homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Assicurati che il nome del layout sia corretto

        // Trova i riferimenti delle viste
        ImageButton profileButtonTop = findViewById(R.id.profileButtonTop);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);

        // Verifica che le viste siano state trovate
        if (homeBackgroundCircle != null && homeButton != null) {
            // Imposta il cerchio di sfondo solo per il pulsante Home
            updateNavSelection(R.id.homeButton);
        } else {
            // Log per il debug (opzionale)
            System.out.println("homeBackgroundCircle o homeButton non trovati nel layout.");
        }

        // Listener per il pulsante Profilo
        profileButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a ProfileActivity
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateNavSelection(int selectedButtonId) {
        // Mostra il cerchio viola solo se è selezionato il pulsante Home
        if (selectedButtonId == R.id.homeButton && homeBackgroundCircle != null) {
            homeBackgroundCircle.setVisibility(View.VISIBLE);
        } else if (homeBackgroundCircle != null) {
            homeBackgroundCircle.setVisibility(View.GONE);
        }
    }
}
