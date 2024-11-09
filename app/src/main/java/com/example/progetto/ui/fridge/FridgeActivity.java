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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FridgeActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, searchButton, fridgeButton, recipeButton, addButton;
    private TextView titleText;

    // Firestore instance and collection reference
    private FirebaseFirestore firestore;
    private CollectionReference itemsCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge); // Ensure the layout file name is correct

        // Initialize Firestore and reference the "items" collection
        firestore = FirebaseFirestore.getInstance();
        itemsCollection = firestore.collection("items");

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
        addButton = findViewById(R.id.addButton);
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

        // Listener for addButton to open AddProductActivity
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FridgeActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

        // Listener for Profile button
        profileButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // Listener for Recipe button
        recipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FridgeActivity.this, RecipeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Listener for Search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FridgeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Example of reading data from Firestore's "items" collection
        loadItemsFromFirestore();
    }

    private void loadItemsFromFirestore() {
        itemsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Handle retrieved documents here
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Log or use the data as needed
                        Log.d("FridgeActivity", "Items loaded successfully from Firestore.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FridgeActivity", "Failed to load items: " + e.getMessage()));
    }
}
