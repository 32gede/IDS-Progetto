package com.example.progetto.ui.home;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.BaseActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.R;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.search.SearchActivity;

public class HomeActivity extends BaseActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, profileButtonTop, storeButton, fridgeButton, recipeButton;
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
        titleText.setText(getString(R.string.home));
        // Verifica che le viste siano state trovate
        if (homeBackgroundCircle != null && homeButton != null) {
            // Imposta il cerchio di sfondo solo per il pulsante Home
            updateNavSelection(R.id.homeButton, homeBackgroundCircle, null, null, null);
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
        // Listener per il pulsante Search
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a HomeActivity
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        // Listener per il pulsante Fridge
        fridgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a FridgeActivity
                Intent intent = new Intent(HomeActivity.this, FridgeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        // Listener per il pulsante Recipe
        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a RecipeActivity
                Intent intent = new Intent(HomeActivity.this, RecipeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
    }


}
