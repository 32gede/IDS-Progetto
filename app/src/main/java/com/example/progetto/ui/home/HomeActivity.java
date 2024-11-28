package com.example.progetto.ui.home;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.PopularRecipeAdapter;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.store.StoreActivity;
import com.example.progetto.data.model.SwipeGestureListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, profileButtonTop, storeButton, fridgeButton, recipeButton, notificationButton;
    private TextView titleText;

    private FirebaseFirestore firestore;
    private RecyclerView popularRecyclerView;
    private RecyclerView newerRecyclerView;
    private RecyclerView cookableRecyclerView;
    private RecipeAdapter adapterPopular;
    private RecipeAdapter adapterNewer;
    private RecipeAdapter adapterCookable;
    private List<Recipe> popularRecipe = new ArrayList<>();
    private List<Recipe> newerRecipe = new ArrayList<>();
    private List<Recipe> cookableRecipe = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Inizializza Firestore
        firestore = FirebaseFirestore.getInstance();

        // Trova i riferimenti delle viste
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
        titleText = findViewById(R.id.title);
        titleText.setText(getString(R.string.home));

        // Inizializza SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadRecipes);

        // Verifica che le viste siano state trovate
        if (homeBackgroundCircle != null && homeButton != null) {
            // Imposta il cerchio di sfondo solo per il pulsante Home
            updateNavSelection(R.id.homeButton, homeBackgroundCircle, null, null, null);
        } else {
            Log.e(TAG, "homeBackgroundCircle o homeButton non trovati nel layout.");
        }

        // Configura RecyclerView
        popularRecyclerView = findViewById(R.id.popularRecyclerView);
        popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterPopular = new RecipeAdapter(popularRecipe, null,this, true);
        popularRecyclerView.setAdapter(adapterPopular);
        newerRecyclerView = findViewById(R.id.newerRecipeRecyclerView);
        newerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterNewer = new RecipeAdapter(newerRecipe,null, this, true);
        newerRecyclerView.setAdapter(adapterNewer);
        cookableRecyclerView = findViewById(R.id.cookableRecyclerView);
        cookableRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterCookable = new RecipeAdapter(cookableRecipe, null, this, true);
        cookableRecyclerView.setAdapter(adapterCookable);

        // Configura pulsanti
        configureNavigation();

        // Carica le ricette consigliate
        loadRecipes();
    }

    private void configureNavigation() {
        // Listener per i pulsanti di navigazione
        profileButtonTop.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        storeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StoreActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        fridgeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FridgeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        recipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RecipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Set up swipe gesture listener
        SwipeGestureListener swipeGestureListener = new SwipeGestureListener(this, StoreActivity.class, HomeActivity.class);
        View mainView = findViewById(R.id.home);
        mainView.setOnTouchListener(swipeGestureListener);
    }

    private void loadRecipes() {
        firestore.collection("recipes")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    popularRecipe.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        popularRecipe.add(recipe);
                    }
                    adapterPopular.notifyDataSetChanged();
                    Log.d(TAG, "Top 5 ricette caricate con successo.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel caricamento delle ricette: " + e.getMessage()));
        firestore.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newerRecipe.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        newerRecipe.add(recipe);
                    }
                    adapterNewer.notifyDataSetChanged();
                    Log.d(TAG, "Ultime 5 ricette caricate con successo.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel caricamento delle ricette: " + e.getMessage()));
        loadCookedRecipes(FirebaseAuth.getInstance().getUid());

        // Stop the refreshing animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadCookedRecipes(String userId) {
        // Load user products
        List<Recipe> globalRecipes = new ArrayList<>();
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    globalRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        globalRecipes.add(recipe);
                    }
                    Log.d("HomeActivity", "Global recipes: " + globalRecipes.size());
                })
                .addOnFailureListener(e -> Log.e("HomeActivity", "Failed to load global recipes: " + e.getMessage()));
        Set<String> userIngredients = new HashSet<>();
        firestore.collection("user_products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userIngredients.clear(); // Clear current product list

                    // Populate the list with product names
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productName = document.getString("name");
                        if (productName != null) {
                            userIngredients.add(productName);
                        }
                    }
                    for (String ingredient : userIngredients) {
                        Log.d("HomeActivity", "User ingredient: " + ingredient);
                    }
                    Log.d("HomeActivity", "User ingredients: " + userIngredients.size());
                    // Filter recipes based on user ingredients
                    cookableRecipe.clear();
                    for (Recipe recipe : globalRecipes) {
                        Log.d("HomeActivity", "Checking recipe: " + recipe.getName());
                        Log.d("HomeActivity", "Checking recipe: " + recipe.getIngredients());
                        List<String> recipeIngredients = Arrays.asList(recipe.getIngredients().split(","));
                        for (int i = 0; i < recipeIngredients.size(); i++) {
                            Log.d("HomeActivity", "Ingredient: " + recipeIngredients.get(i));
                        }
                        if (userIngredients.containsAll(recipeIngredients)) {
                            cookableRecipe.add(recipe);
                        }
                    }
                    Log.d("HomeActivity", "Cooked recipes size: " + cookableRecipe.size());
                    adapterCookable.setRecipes(cookableRecipe);

                })
                .addOnFailureListener(e -> Log.e("HomeActivity", "Failed to load user products: " + e.getMessage()));

    }

    private interface OnFridgeItemsLoadedListener {
        void onFridgeItemsLoaded(List<String> fridgeItems);
    }
}