package com.example.progetto.ui.store;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.StoreAdapter;
import com.example.progetto.data.model.StoreUtils;
import com.example.progetto.data.model.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StoreActivity extends AppCompatActivity {

    private static final String TAG = "StoreActivity";

    // UI components
    private TextView titleText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private FloatingActionButton addButton;

    // Adapter and Data
    private StoreAdapter adapter;
    private List<StoreUtils> globalStores = new ArrayList<>();
    private List<StoreUtils> savedStores = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.store_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup UI
        initializeViews();
        setupRecyclerView();
        setupTabLayout();
        setupSwipeRefresh();
        validateUserStores();
        loadStores();
    }

    private void initializeViews() {
        NavigationHelper.setupToolbar(findViewById(R.id.profileButton), findViewById(R.id.notificationButton), this);
        titleText = findViewById(R.id.title);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutStore);
        recyclerView = findViewById(R.id.recyclerViewStore);
        tabLayout = findViewById(R.id.tabLayoutStore);
        addButton = findViewById(R.id.addButtonStore);
        titleText.setText(getString(R.string.search));
        addButton.setOnClickListener(v -> navigateTo(AddStoreActivity.class));
    }

    private void setupRecyclerView() {
        adapter = new StoreAdapter(new ArrayList<>(), this, tabLayout.getSelectedTabPosition(), false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Set GridLayoutManager with 2 columns
        recyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Saved"));
        tabLayout.addTab(tabLayout.newTab().setText("Global"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.setStores(savedStores);
                    addButton.setOnClickListener(v -> navigateTo(AddStoreActivity.class));
                    addButton.setImageResource(R.drawable.baseline_add_24);
                } else {
                    adapter.setStores(globalStores);
                    addButton.setImageResource(R.drawable.baseline_shopping_basket_24);
                    addButton.setOnClickListener(v -> navigateTo(CartActivity.class));
                }
                adapter.setSelectedTabPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadStores();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(StoreActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void validateUserStores() {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("user_store")
                .whereEqualTo("buyer_id", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storeId = document.getString("store_id");

                            db.collection("stores").document(storeId).get()
                                    .addOnSuccessListener(storeDoc -> {
                                        if (!storeDoc.exists()) {
                                            db.collection("user_store").document(document.getId()).delete()
                                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed invalid store from user_store"))
                                                    .addOnFailureListener(e -> Log.e(TAG, "Error removing invalid store from user_store", e));
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error checking store existence", e));
                        }
                    } else {
                        Log.e(TAG, "Error getting user stores: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to validate user stores: ", e));
    }

    private void loadStores() {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("stores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        globalStores.clear();
                        savedStores.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StoreUtils store = document.toObject(StoreUtils.class);
                            if (store.getUserId().equals(currentUserId)) {
                                savedStores.add(store);
                            } else {
                                globalStores.add(store);
                            }
                        }

                        if (tabLayout.getSelectedTabPosition() == 0) {
                            adapter.setStores(savedStores);
                        } else {
                            adapter.setStores(globalStores);
                        }
                    } else {
                        Log.e(TAG, "Error getting stores: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load stores: ", e));
    }
}