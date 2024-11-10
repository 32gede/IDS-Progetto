package com.example.progetto.ui.fridge;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
    private List<ItemUtils> filteredList = new ArrayList<>();

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
        setContentView(R.layout.fridge); // Assicurati che il nome del file di layout sia corretto

        // Inizializza Firestore e riferimento alla collezione "items"
        firestore = FirebaseFirestore.getInstance();
        itemsCollection = firestore.collection("items");
        mAuth = FirebaseAuth.getInstance();

        // Inizializza RecyclerView
        recyclerViewFridge = findViewById(R.id.recyclerViewFridge);
        recyclerViewFridge.setLayoutManager(new GridLayoutManager(this, 2)); // Imposta il layout manager come griglia a 2 colonne
        productAdapter = new ProductAdapter(this, fridgeProductList, null); // Non serve un listener di selezione in FridgeActivity
        recyclerViewFridge.setAdapter(productAdapter);

        // Trova e configura i riferimenti alle altre view
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

        // Evidenzia il pulsante "fridge" nella navbar
        updateNavSelection(R.id.fridgeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);

        // Imposta i listener per i pulsanti di navigazione
        setNavigationListeners(profileButtonTop);

        // Carica gli elementi da Firestore
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
        Log.d("FridgeActivity", "User ID: " + userId);

        if (userId == null) {
            Log.e("FridgeActivity", "User ID is null. Cannot load items.");
            return;
        }

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
                    Log.d("FridgeActivity", "User Product IDs: " + userProductIds);

                    // Retrieve product details based on IDs
                    if (!userProductIds.isEmpty()) {
                        itemsCollection.get()
                                .addOnSuccessListener(productSnapshots -> {
                                    fridgeProductList.clear();
                                    for (QueryDocumentSnapshot productDoc : productSnapshots) {
                                        String productId = productDoc.getId();
                                        Log.d("FridgeActivity", "Checking product ID: " + productId);

                                        if (userProductIds.contains(productId)) {
                                            // Only add the product if it's in userProductIds
                                            ItemUtils product = productDoc.toObject(ItemUtils.class);
                                            product.setId(productId); // Set ID explicitly
                                            fridgeProductList.add(product);
                                        }
                                    }

                                    // Update the filtered list and RecyclerView
                                    filteredList.clear();
                                    filteredList.addAll(fridgeProductList);
                                    productAdapter.updateProductList(filteredList);
                                    Log.d("FridgeActivity", "Items loaded successfully from Firestore. Total: " + fridgeProductList.size());
                                })
                                .addOnFailureListener(e -> Log.e("FridgeActivity", "Failed to load product details: " + e.getMessage()));
                    } else {
                        // Handle case where there are no products for the user
                        fridgeProductList.clear();
                        productAdapter.updateProductList(fridgeProductList);
                        Log.d("FridgeActivity", "No products found for the user.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FridgeActivity", "Failed to load user products: " + e.getMessage()));
    }

}
