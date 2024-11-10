package com.example.progetto.ui.fridge;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.adapter.ProductAdapter;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    // Firestore instance and collection reference for "items"
    private FirebaseFirestore firestore;
    private CollectionReference itemsCollection;

    private ProductAdapter productAdapter;
    private List<ItemUtils> productList = new ArrayList<>();
    private List<ItemUtils> filteredList = new ArrayList<>(); // For storing filtered products

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
        productAdapter = new ProductAdapter(this, filteredList); // Use filteredList for adapter
        recyclerView.setAdapter(productAdapter);

        // Load items from Firestore
        loadItemsFromFirestore();

        // Set up back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        // Setup search bar
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
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
                    // Initialize filtered list with all items initially
                    filteredList.clear();
                    filteredList.addAll(productList);
                    productAdapter.updateProductList(filteredList);
                    Log.d("AddProductActivity", "Items loaded successfully from Firestore.");
                })
                .addOnFailureListener(e -> Log.e("AddProductActivity", "Failed to load items: " + e.getMessage()));
    }

    // Filter products based on search input
    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            // Show all products if search query is empty
            filteredList.addAll(productList);
        } else {
            // Filter based on the search query
            for (ItemUtils product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.updateProductList(filteredList); // Update adapter with filtered list
    }
}
