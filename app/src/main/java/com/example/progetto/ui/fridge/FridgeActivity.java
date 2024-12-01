package com.example.progetto.ui.fridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.UserProductAdapter;
import com.example.progetto.data.model.UserProductUtils;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.data.model.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FridgeActivity extends AppCompatActivity {

    private ImageButton addButton, notificationButton;
    private TextView titleText;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerViewFridge;
    private UserProductAdapter productAdapter;
    private List<UserProductUtils> fridgeProductList = new ArrayList<>();
    private List<UserProductUtils> filteredList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge);

        // Configura il BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.fridge_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Inizializza Firestore e Auth
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inizializza RecyclerView
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewFridge = findViewById(R.id.recyclerViewFridge);
        recyclerViewFridge.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new UserProductAdapter(this, fridgeProductList, null);
        recyclerViewFridge.setAdapter(productAdapter);

        // Inizializza le viste
        initializeViews();

        // Configura i listener di navigazione
        setNavigationListeners();

        // Configura SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadItemsFromFirestore);

        // Carica i prodotti da Firestore
        loadItemsFromFirestore();
    }

    private void initializeViews() {
        NavigationHelper.setupToolbar(findViewById(R.id.profileButton), findViewById(R.id.notificationButton), this);
        addButton = findViewById(R.id.addButton);
        titleText = findViewById(R.id.title);
        titleText.setText(getString(R.string.fridge));}

    private void setNavigationListeners() {
        addButton.setOnClickListener(v -> startActivity(new Intent(FridgeActivity.this, AddProductActivity.class)));
    }

    private void loadItemsFromFirestore() {
        swipeRefreshLayout.setRefreshing(true);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        Log.d("FridgeActivity", "User ID: " + userId);

        if (userId == null) {
            Log.e("FridgeActivity", "User ID is null. Cannot load items.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        firestore.collection("user_products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fridgeProductList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserProductUtils userProduct = document.toObject(UserProductUtils.class);
                        fridgeProductList.add(userProduct);
                    }

                    filteredList.clear();
                    filteredList.addAll(fridgeProductList);
                    productAdapter.updateProductList(filteredList);
                    Log.d("FridgeActivity", "Items loaded successfully from Firestore. Total: " + fridgeProductList.size());

                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("FridgeActivity", "Failed to load user products: " + e.getMessage());
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    public List<String> getFridgeItems() {
        List<String> fridgeItems = new ArrayList<>();
        for (UserProductUtils product : fridgeProductList) {
            fridgeItems.add(product.getName());
        }
        return fridgeItems;
    }
}
