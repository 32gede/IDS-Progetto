package com.example.progetto.ui.fridge;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.adapter.ProductAdapter;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.R;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, searchButton, fridgeButton, recipeButton;
    private TextView titleText;

    // Firestore instance and collection reference for "items"
    private FirebaseFirestore firestore;
    private CollectionReference itemsCollection;

    private ProductAdapter productAdapter;
    private List<ItemUtils> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_fridge);

        // Initialize Firestore and reference the "items" collection
        firestore = FirebaseFirestore.getInstance();
        itemsCollection = firestore.collection("items");

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);

        // Load items from Firestore
        loadItemsFromFirestore();

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
            updateNavSelection(R.id.fridgeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);
        } else {
            Log.e("FridgeActivity", "One or more views (homeBackgroundCircle or homeButton) not found in the layout.");
        }

        // Listener for Profile button
        profileButtonTop.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Listener for Home button
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Listener for Recipe button
        recipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, RecipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Listener for Search button
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private void loadItemsFromFirestore() {
        itemsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ItemUtils product = document.toObject(ItemUtils.class);
                        productList.add(product);
                    }
                    productAdapter.updateProductList(productList);
                    Log.d("AddProductActivity", "Items loaded successfully from Firestore.");
                })
                .addOnFailureListener(e -> Log.e("AddProductActivity", "Failed to load items: " + e.getMessage()));
    }
}
