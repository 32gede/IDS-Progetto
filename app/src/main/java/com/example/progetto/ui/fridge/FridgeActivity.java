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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.adapter.UserProductAdapter;
import com.example.progetto.data.model.UserProductUtils;
import com.example.progetto.R;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FridgeActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, searchButton, fridgeButton, recipeButton, addButton;
    private TextView titleText;

    // Firebase Firestore and Authentication
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    // RecyclerView and Adapter for displaying user products
    private RecyclerView recyclerViewFridge;
    private UserProductAdapter productAdapter;
    private List<UserProductUtils> fridgeProductList = new ArrayList<>();
    private List<UserProductUtils> filteredList = new ArrayList<>();

    // SwipeRefreshLayout for pull-to-refresh functionality
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge);

        // Initialize Firestore and Authentication
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and Adapter
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewFridge = findViewById(R.id.recyclerViewFridge);
        recyclerViewFridge.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new UserProductAdapter(this, fridgeProductList, null);
        recyclerViewFridge.setAdapter(productAdapter);

        // Initialize view references
        initializeViews();

        // Highlight "fridge" button in the navbar
        updateNavSelection(R.id.fridgeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);

        // Set navigation listeners
        setNavigationListeners();

        // Set up SwipeRefreshLayout to refresh on swipe down
        swipeRefreshLayout.setOnRefreshListener(this::loadItemsFromFirestore);

        // Load items from Firestore initially
        loadItemsFromFirestore();
    }

    private void initializeViews() {
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
        profileButtonTop.setOnClickListener(v -> startActivity(new Intent(FridgeActivity.this, ProfileActivity.class)));
    }

    private void setNavigationListeners() {
        addButton.setOnClickListener(v -> startActivity(new Intent(FridgeActivity.this, AddProductActivity.class)));
        homeButton.setOnClickListener(v -> navigateToActivity(HomeActivity.class));
        recipeButton.setOnClickListener(v -> navigateToActivity(RecipeActivity.class));
        searchButton.setOnClickListener(v -> navigateToActivity(SearchActivity.class));
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(FridgeActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void loadItemsFromFirestore() {
        // Start refreshing animation if not already started
        swipeRefreshLayout.setRefreshing(true);

        // Retrieve user ID to filter specific products
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        Log.d("FridgeActivity", "User ID: " + userId);

        if (userId == null) {
            Log.e("FridgeActivity", "User ID is null. Cannot load items.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Query to "user_products" collection to get only products associated with the user
        firestore.collection("user_products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fridgeProductList.clear(); // Clear current product list

                    // Populate the list with UserProductUtils objects
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserProductUtils userProduct = document.toObject(UserProductUtils.class);
                        fridgeProductList.add(userProduct);
                    }

                    // Update the filtered list and RecyclerView
                    filteredList.clear();
                    filteredList.addAll(fridgeProductList);
                    productAdapter.updateProductList(filteredList);
                    Log.d("FridgeActivity", "Items loaded successfully from Firestore. Total: " + fridgeProductList.size());

                    // Stop the refreshing animation
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("FridgeActivity", "Failed to load user products: " + e.getMessage());
                    swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation on failure
                });
    }
}
