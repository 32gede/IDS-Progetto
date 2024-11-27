// StoreActivity.java
package com.example.progetto.ui.store;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.StoreAdapter;
import com.example.progetto.data.model.StoreUtils;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
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
    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, profileButtonTop, fridgeButton, recipeButton, notificationButton;
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

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup UI
        initializeViews();
        setupRecyclerView();
        setupTabLayout();
        setupSwipeRefresh();
        loadStores();

        // Set navigation selection
        updateNavSelection(R.id.storeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);

        // Setup button listeners
        setupNavigationListeners();
    }

    private void initializeViews() {
        profileButtonTop = findViewById(R.id.profileButtonTop);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        searchBackgroundCircle = findViewById(R.id.searchBackgroundCircle);
        fridgeBackgroundCircle = findViewById(R.id.fridgeBackgroundCircle);
        recipeBackgroundCircle = findViewById(R.id.recipeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);
        fridgeButton = findViewById(R.id.fridgeButton);
        recipeButton = findViewById(R.id.recipeButton);
        titleText = findViewById(R.id.title);
        notificationButton = findViewById(R.id.notificationButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutStore);
        recyclerView = findViewById(R.id.recyclerViewStore);
        tabLayout = findViewById(R.id.tabLayoutStore);
        addButton = findViewById(R.id.addButtonStore);

        titleText.setText(getString(R.string.search));
    }

    private void setupRecyclerView() {
        adapter = new StoreAdapter(new ArrayList<>(), this, tabLayout.getSelectedTabPosition(), false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void handleStoreClick(StoreUtils store) {
        Log.d(TAG, "Store clicked: " + store.getName());
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Saved"));
        tabLayout.addTab(tabLayout.newTab().setText("Global"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.setStores(savedStores);
                    addButton.setImageResource(R.drawable.baseline_add_24);
                    addButton.setOnClickListener(v -> navigateTo(AddStoreActivity.class));
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

    private void setupNavigationListeners() {
        profileButtonTop.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        homeButton.setOnClickListener(v -> navigateTo(HomeActivity.class));
        fridgeButton.setOnClickListener(v -> navigateTo(FridgeActivity.class));
        recipeButton.setOnClickListener(v -> navigateTo(RecipeActivity.class));
        notificationButton.setOnClickListener(v -> navigateTo(NotificationActivity.class));

    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(StoreActivity.this, activityClass);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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