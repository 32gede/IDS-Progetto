package com.example.progetto.data.model;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.ui.recipe.RecipeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.progetto.R;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.store.StoreActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.profile.ProfileActivity;

public abstract class BaseBottomNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation();
    }

    /**
     * Configura il BottomNavigationView e gestisce il cambio attività.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            throw new IllegalStateException("Devi includere un BottomNavigationView con ID 'bottom_navigation' nel layout.");
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                handleNavigation(item);
                return true;
            }
        });
    }

    /**
     * Gestisce la navigazione tra le attività in base all'elemento selezionato.
     *
     * @param item il MenuItem selezionato
     */
    private void handleNavigation(MenuItem item) {
        item.getItemId();
        if(item.getItemId() == R.id.home_button){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Nessuna animazione di transizione
            finish();
        }
        if(item.getItemId() == R.id.store_button){
            Intent intent = new Intent(this, StoreActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Nessuna animazione di transizione
            finish();
        }
        if(item.getItemId() == R.id.fridge_button){
            Intent intent = new Intent(this, FridgeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Nessuna animazione di transizione
            finish();
        }
        if(item.getItemId() == R.id.recipe_button){
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Nessuna animazione di transizione
            finish();
        }
    }


    /**
     * Metodo da sovrascrivere per indicare l'ID del menu selezionato correntemente.
     *
     * @return l'ID dell'elemento selezionato
     */
    protected abstract int getCurrentMenuItemId();

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(getCurrentMenuItemId());
        }
    }
}
