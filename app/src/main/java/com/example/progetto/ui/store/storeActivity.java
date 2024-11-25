package com.example.progetto.ui.store;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;

public class storeActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, profileButtonTop, storeButton, fridgeButton, recipeButton,notificationButton;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Assicurati che il nome del layout sia corretto

        // Trova i riferimenti delle viste
        profileButtonTop = findViewById(R.id.profileButtonTop);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        searchBackgroundCircle = findViewById(R.id.searchBackgroundCircle);
        fridgeBackgroundCircle = findViewById(R.id.fridgeBackgroundCircle);
        recipeBackgroundCircle = findViewById(R.id.recipeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);
        storeButton = findViewById(R.id.storeButton);
        fridgeButton = findViewById(R.id.fridgeButton);
        recipeButton = findViewById(R.id.recipeButton);
        titleText = findViewById(R.id.title);
        titleText.setText(getString(R.string.search));
        notificationButton = findViewById(R.id.notificationButton);


        // Verifica che le viste siano state trovate
        if (homeBackgroundCircle != null && homeButton != null) {
            // Imposta il cerchio di sfondo solo per il pulsante Home
            updateNavSelection(R.id.storeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);
        } else {
            // Log per il debug (opzionale)
            System.out.println("homeBackgroundCircle o homeButton non trovati nel layout.");
        }

        // Listener per il pulsante Profilo
        profileButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a ProfileActivity
                Intent intent = new Intent(storeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        // Listener per il pulsante Home
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a HomeActivity
                Intent intent = new Intent(storeActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
        // Listener per il pulsante Fridge
        fridgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a FridgeActivity
                Intent intent = new Intent(storeActivity.this, FridgeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
        // Listener per il pulsante Recipe
        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a RecipeActivity
                Intent intent = new Intent(storeActivity.this, RecipeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a NotificationActivity
                Intent intent = new Intent(storeActivity.this, NotificationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
    }


}