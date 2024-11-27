package com.example.progetto.ui.store;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.StoreAdapter;
import com.example.progetto.data.model.StoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    private RecyclerView recyclerView;
    private StoreAdapter adapter;
    private Button checkoutButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView back_button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerView);
        checkoutButton = findViewById(R.id.buy_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StoreAdapter(new ArrayList<>(), this, 1, true); // Assuming true for isCartActivity
        recyclerView.setAdapter(adapter);

        loadUserStores();

        checkoutButton.setOnClickListener(v -> checkout());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUserStores();
        });
    }

    private void loadUserStores() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("user_store")
                .whereEqualTo("buyer_id", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> storeIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            storeIds.add(document.getString("store_id"));
                        }
                        Log.d(TAG, "User store IDs: " + storeIds);
                        loadStores(storeIds);
                    } else {
                        Log.e(TAG, "Error getting user stores: ", task.getException());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user stores: ", e);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void loadStores(List<String> storeIds) {
        if (storeIds.isEmpty()) {
            Log.d(TAG, "No stores found for the user.");
            adapter.setStores(new ArrayList<>());
            return;
        }

        db.collection("stores")
                .whereIn("id", storeIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<StoreUtils> stores = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StoreUtils store = document.toObject(StoreUtils.class);
                            stores.add(store);
                        }
                        Log.d(TAG, "Stores loaded: " + stores);
                        adapter.setStores(stores);
                    } else {
                        Log.e(TAG, "Error getting stores: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load stores: ", e));
    }

    private void checkout() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("user_store")
                .whereEqualTo("buyer_id", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storeId = document.getString("store_id");

                            // Check if the store item is still available
                            db.collection("stores").document(storeId).get()
                                    .addOnSuccessListener(storeDoc -> {
                                        if (storeDoc.exists()) {
                                            // Proceed with checkout
                                            deleteUserStoreAndStore(document.getId(), storeId);
                                        } else {
                                            // Store item is no longer available, remove from user cart
                                            db.collection("user_store").document(document.getId()).delete()
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Store removed from user_store");
                                                        Toast.makeText(CartActivity.this, "Box non più disponibile", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> Log.e(TAG, "Error removing store from user_store", e));
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error checking store availability", e));
                        }
                    } else {
                        Log.e(TAG, "Error getting user stores for checkout: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get user stores for checkout: ", e));
    }

    private void deleteUserStoreAndStore(String userStoreId, String storeId) {
        // Delete from user_store
        db.collection("user_store").document(userStoreId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted from user_store"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting from user_store", e));

        // Delete from stores
        db.collection("stores").document(storeId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Deleted from stores"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting from stores", e));

        Toast.makeText(CartActivity.this, "Purchase successful!", Toast.LENGTH_SHORT).show();

        // Navigate back to StoreActivity
        Intent intent = new Intent(CartActivity.this, StoreActivity.class);
        startActivity(intent);
        finish();
    }
}