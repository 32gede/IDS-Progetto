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

public class storeActivity extends AppCompatActivity {

    private static final String TAG = "storeActivity";

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, profileButtonTop, storeButton, fridgeButton, recipeButton, notificationButton;
    private TextView titleText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private FloatingActionButton addButton;

    private StoreAdapter adapter;
    private List<StoreUtils> globalStores;
    private List<StoreUtils> savedStores;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store); // Assicurati che il nome del layout sia corretto

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        findView();
        setupRecyclerView();
        setupTabLayout();
        setupSwipeRefresh();
        loadStores(); // Load stores from Firestore

        // Verifica che le viste siano state trovate
        if (homeBackgroundCircle != null && homeButton != null) {
            // Imposta il cerchio di sfondo solo per il pulsante Home
            updateNavSelection(R.id.storeButton, homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle);
        } else {
            // Log per il debug
            Log.e(TAG, "homeBackgroundCircle o homeButton non trovati nel layout.");
        }

        // Listener per il pulsante Profilo
        profileButtonTop.setOnClickListener(v -> {
            // Naviga a ProfileActivity
            Intent intent = new Intent(storeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Listener per il pulsante Home
        homeButton.setOnClickListener(v -> {
            // Naviga a HomeActivity
            Intent intent = new Intent(storeActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Listener per il pulsante Fridge
        fridgeButton.setOnClickListener(v -> {
            // Naviga a FridgeActivity
            Intent intent = new Intent(storeActivity.this, FridgeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Listener per il pulsante Recipe
        recipeButton.setOnClickListener(v -> {
            // Naviga a RecipeActivity
            Intent intent = new Intent(storeActivity.this, RecipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Listener per il pulsante Notifiche
        notificationButton.setOnClickListener(v -> {
            // Naviga a NotificationActivity
            Intent intent = new Intent(storeActivity.this, NotificationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Listener per il pulsante Aggiungi
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(storeActivity.this, AddStoreActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        globalStores = new ArrayList<>();
        savedStores = new ArrayList<>();
        adapter = new StoreAdapter(new ArrayList<>(), (store, isSaved) -> {
            if (isSaved) {
                saveStoreToUserCollection(store); // Save the store
            } else {
                removeStoreFromUserCollection(store); // Remove the store
            }
        }, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Salvati"));
        tabLayout.addTab(tabLayout.newTab().setText("Globali"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                // Handle tab selection
                Log.d(TAG, "Tab selected: " + tab.getPosition());
                if (tab.getPosition() == 0) {
                    adapter.setStores(new ArrayList<>(savedStores));
                } else {
                    adapter.setStores(globalStores);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab unselected: " + tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab reselected: " + tab.getPosition());
            }
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh data
            Log.d(TAG, "Swipe to refresh triggered");
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void findView() {
        profileButtonTop = findViewById(R.id.profileButtonTop);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        searchBackgroundCircle = findViewById(R.id.searchBackgroundCircle);
        fridgeBackgroundCircle = findViewById(R.id.fridgeBackgroundCircle);
        recipeBackgroundCircle = findViewById(R.id.recipeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);
        storeButton = findViewById(R.id.storeButton);
        fridgeButton = findViewById(R.id.fridgeButton);
        recipeButton = findViewById(R.id.recipeButton);
        titleText = findViewById(R.id.title);
        notificationButton = findViewById(R.id.notificationButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutStore);
        recyclerView = findViewById(R.id.recyclerViewStore);
        tabLayout = findViewById(R.id.tabLayoutStore);
        addButton = findViewById(R.id.addButtonStore);

        titleText.setText(getString(R.string.search));

        // Log for view initialization
        Log.d(TAG, "Views initialized");
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

                        Log.e(TAG, "Saved stores size: "+ savedStores.size());
                        Log.e(TAG, "Global stores size: "+ globalStores.size());

                        // Update the adapter based on the selected tab
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            adapter.setStores(new ArrayList<>(savedStores));
                        } else {
                            adapter.setStores(new ArrayList<>(globalStores));
                        }
                    } else {
                        Log.e(TAG, "Error getting stores: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load stores: ", e));
    }

    private void saveStoreToUserCollection(StoreUtils store) {
        // Implement the logic to save the store to the user's collection
        Log.d(TAG, "Saving store to user collection: " + store.getId());
    }


    private void removeStoreFromUserCollection(StoreUtils store) {
        // Implement the logic to remove the store from the user's collection
        Log.d(TAG, "Removing store from user collection: " + store.getId());
    }
}