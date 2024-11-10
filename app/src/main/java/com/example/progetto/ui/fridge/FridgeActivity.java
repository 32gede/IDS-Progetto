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
import com.example.progetto.R;
import com.example.progetto.data.model.UserProductUtils;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FridgeActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, searchButton, fridgeButton, recipeButton, addButton;
    private TextView titleText;

    // Firestore instance and collection reference
    private FirebaseFirestore firestore;
    private CollectionReference itemsCollection;
    private FirebaseAuth mAuth;  // Firebase Authentication instance

    // RecyclerView and Adapter for displaying products
    private RecyclerView recyclerViewFridge;
    private ProductAdapter productAdapter;
    private List<ItemUtils> fridgeProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge); // Ensure the layout file name is correct

        // Initialize Firestore and reference the "items" collection
        firestore = FirebaseFirestore.getInstance();
        itemsCollection = firestore.collection("items");
        mAuth = FirebaseAuth.getInstance();


        // Initialize RecyclerView and Adapter
        recyclerViewFridge = findViewById(R.id.recyclerViewFridge);
        recyclerViewFridge.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, fridgeProductList, null); // No selection listener needed in FridgeActivity
        recyclerViewFridge.setAdapter(productAdapter);

        // Find and set up other view references
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

        // Highlight the fridge button in the navigation bar
        updateNavSelection(R.id.fridgeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);

        // Set listeners for navigation buttons
        setNavigationListeners(profileButtonTop);

        // Load items from Firestore
        loadItemsFromFirestore();
    }

    private void setNavigationListeners(ImageButton profileButtonTop) {
        // Listener for Add button to open AddProductActivity
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(FridgeActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        // Listener for Profile button
        profileButtonTop.setOnClickListener(v -> {
            Intent intent = new Intent(FridgeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Listener for Home button
        homeButton.setOnClickListener(v -> navigateToActivity(HomeActivity.class));

        // Listener for Recipe button
        recipeButton.setOnClickListener(v -> navigateToActivity(RecipeActivity.class));

        // Listener for Search button
        searchButton.setOnClickListener(v -> navigateToActivity(SearchActivity.class));
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(FridgeActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void loadItemsFromFirestore() {
        // Supponiamo di avere un ID utente (es. userId) per filtrare i prodotti specifici dell'utente
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Primo passo: recuperare i prodotti associati all'utente
        firestore.collection("user_products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userProductIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserProductUtils userProduct = document.toObject(UserProductUtils.class);
                        userProductIds.add(userProduct.getProductId());
                    }

                    // Secondo passo: recuperare i dettagli dei prodotti basati sugli ID
                    if (!userProductIds.isEmpty()) {
                        itemsCollection.whereIn("id", userProductIds)
                                .get()
                                .addOnSuccessListener(productSnapshots -> {
                                    fridgeProductList.clear();
                                    for (QueryDocumentSnapshot productDoc : productSnapshots) {
                                        ItemUtils product = productDoc.toObject(ItemUtils.class);
                                        product.setId(productDoc.getId()); // Assicurati che l'ID sia impostato
                                        fridgeProductList.add(product);
                                    }
                                    // Aggiornare la RecyclerView con i dati recuperati
                                    productAdapter.updateProductList(fridgeProductList);
                                    Log.d("FridgeActivity", "Items loaded successfully from Firestore.");
                                })
                                .addOnFailureListener(e -> Log.e("FridgeActivity", "Failed to load product details: " + e.getMessage()));
                    } else {
                        // Gestisci il caso in cui non ci sono prodotti per l'utente
                        fridgeProductList.clear();
                        productAdapter.updateProductList(fridgeProductList);
                        Log.d("FridgeActivity", "No products found for the user.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FridgeActivity", "Failed to load user products: " + e.getMessage()));
    }

}
