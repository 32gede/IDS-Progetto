package com.example.progetto.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.NavigationHelper;
import com.example.progetto.data.model.SelectedIngredientRecipeUtils;
import com.example.progetto.data.model.UserRecipeUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private RecipeAdapter adapterPopular;
    private RecipeAdapter adapterNewer;
    private RecipeAdapter adapterCookable;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Firebase
    private Firestore firestore;

    // Data
    private final List<Recipe> popularRecipe = new ArrayList<>();
    private final List<Recipe> newerRecipe = new ArrayList<>();
    private final List<Recipe> cookableRecipe = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        firestore = new Firestore();

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.home_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Initialize Firestore

        // Initialize Views
        initializeViews();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load Recipes
        loadRecipes();
    }

    private void initializeViews() {
        TextView titleText = findViewById(R.id.title);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        titleText.setText(getString(R.string.home));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh triggered");
            loadRecipes();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecyclerViews() {
        // UI Components
        RecyclerView popularRecyclerView = findViewById(R.id.popularRecyclerView);
        RecyclerView newerRecyclerView = findViewById(R.id.newerRecipeRecyclerView);
        RecyclerView cookableRecyclerView = findViewById(R.id.cookableRecyclerView);

        NavigationHelper.setupToolbar(findViewById(R.id.profileButton), findViewById(R.id.notificationButton), this);

        adapterPopular = new RecipeAdapter(popularRecipe, null, this, true);
        adapterNewer = new RecipeAdapter(newerRecipe, null, this, true);
        adapterCookable = new RecipeAdapter(cookableRecipe, null, this, true);

        setupRecyclerView(popularRecyclerView, adapterPopular);
        setupRecyclerView(newerRecyclerView, adapterNewer);
        setupRecyclerView(cookableRecyclerView, adapterCookable);
    }

    private void setupRecyclerView(RecyclerView recyclerView, RecipeAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void loadRecipes() {
        Log.d(TAG, "Loading recipes");
        loadPopularRecipes();
        loadNewerRecipes();
        loadCookableRecipes(FirebaseAuth.getInstance().getUid());
    }

    private void loadPopularRecipes() {
        Log.d(TAG, "Loading popular recipes");
        firestore.loadPopularRecipes(new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                popularRecipe.clear();
                popularRecipe.addAll(recipes.subList(0, Math.min(recipes.size(), 5)));
                adapterPopular.changeElements(popularRecipe);
                Log.d(TAG, "Popular recipes loaded: " + popularRecipe.size());
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("Errore durante il caricamento delle ricette: " + e.getMessage());
            }
        });
    }

    private void loadNewerRecipes() {
        Log.d(TAG, "Loading newer recipes");

        firestore.loadNewerRecipes(new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                newerRecipe.clear();
                newerRecipe.addAll(recipes.subList(0, Math.min(recipes.size(), 5)));
                adapterNewer.changeElements(newerRecipe);
                Log.d(TAG, "Newer recipes loaded: " + newerRecipe.size());
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("Errore durante il caricamento delle ricette: " + e.getMessage());
            }
        });
    }

    private void loadCookableRecipes(String userId) {
        Log.d(TAG, "Filtering cookable recipes for user: " + userId);
        firestore.loadCookableRecipes(userId, new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<UserRecipeUtils> cookableRecipesAppo) {
                // Stampa tutte le ricette cookable
                cookableRecipe.addAll(cookableRecipesAppo.subList(0, Math.min(cookableRecipesAppo.size(), 5)));
                Log.d(TAG, "Cookable recipes loaded: " + cookableRecipe.size());
                adapterCookable.changeElements(cookableRecipe);
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("Errore durante il caricamento delle ricette cookable: " + e.getMessage());
            }
        });
    }
}