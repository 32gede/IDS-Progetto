package com.example.progetto.ui.fridge;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.R;
import com.example.progetto.data.model.NavigationUtils;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FridgeActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, searchButton, fridgeButton, recipeButton;
    private TextView titleText;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Ensure the layout file name is correct
        databaseReference = FirebaseDatabase.getInstance().getReference("items");

        // Find view references
        ImageButton profileButtonTop = findViewById(R.id.profileButtonTop);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        searchBackgroundCircle = findViewById(R.id.searchBackgroundCircle);
        fridgeBackgroundCircle = findViewById(R.id.fridgeBackgroundCircle);
        recipeBackgroundCircle = findViewById(R.id.recipeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);
        searchButton = findViewById(R.id.searchButton);
        fridgeButton = findViewById(R.id.fridgeButton);
        recipeButton = findViewById(R.id.recipeButton);
        titleText = findViewById(R.id.title);
        titleText.setText(getString(R.string.fridge));

        // Check if views were found
        if (homeBackgroundCircle != null && homeButton != null) {
            // Set background circle only for the Home button
            updateNavSelection(R.id.fridgeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);
        } else {
            // Log for debugging
            Log.e("FridgeActivity", "One or more views (homeBackgroundCircle or homeButton) not found in the layout.");
        }

        // Listener for Profile button
        profileButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ProfileActivity
                Intent intent = new Intent(FridgeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        // Listener for Home button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FridgeActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
        // Listener per il pulsante Fridge
        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a FridgeActivity
                Intent intent = new Intent(FridgeActivity.this, RecipeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
        // Listener per il pulsante Recipe
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviga a RecipeActivity
                Intent intent = new Intent(FridgeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                finish();
            }
        });
    }
}
