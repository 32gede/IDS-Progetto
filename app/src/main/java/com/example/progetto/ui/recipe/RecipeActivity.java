package com.example.progetto.ui.recipe;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeActivity extends AppCompatActivity {

    // UI Components
    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, searchButton, fridgeButton, recipeButton;
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Firebase and Adapter
    private FirebaseFirestore firestore;
    private RecipeAdapter adapter;

    // Recipe Data
    private List<Recipe> savedRecipes;
    private List<Recipe> globalRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data and configure tabs
        setupSampleData();
        setupTabLayout();

        // Setup navigation buttons
        setNavigationListeners();

        // Setup SwipeRefreshLayout
        setupSwipeRefresh();

        // Update navigation UI
        updateNavSelection(
                R.id.recipeButton,
                homeBackgroundCircle,
                searchBackgroundCircle,
                fridgeBackgroundCircle,
                recipeBackgroundCircle
        );
    }

    private void initializeViews() {
        // Find UI components
        tabLayout = findViewById(R.id.tabLayoutRecipe);
        recyclerView = findViewById(R.id.recyclerViewRecipes);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutRecipe);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        searchBackgroundCircle = findViewById(R.id.searchBackgroundCircle);
        fridgeBackgroundCircle = findViewById(R.id.fridgeBackgroundCircle);
        recipeBackgroundCircle = findViewById(R.id.recipeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);
        searchButton = findViewById(R.id.searchButton);
        fridgeButton = findViewById(R.id.fridgeButton);
        recipeButton = findViewById(R.id.recipeButton);
        addButton = findViewById(R.id.addButtonRecipe);

        // Profile button
        ImageButton profileButtonTop = findViewById(R.id.profileButtonTop);
        if (profileButtonTop != null) {
            profileButtonTop.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void setupRecyclerView() {
        if (recyclerView == null) {
            Log.e("RecipeActivity", "RecyclerView is null. Check R.id.recyclerViewRecipes in recipe.xml.");
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with a bookmark click listener
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            // Save the recipe to Firebase Firestore
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

            Map<String, Object> recipe1 = new HashMap<>();
            recipe1.put("name", recipe.getName());
            recipe1.put("description", recipe.getDescription());
            recipe1.put("ingredients", recipe.getIngredients());
            recipe1.put("steps", recipe.getSteps());
            recipe1.put("image", recipe.getImage()); // Può essere null
            recipe1.put("difficulty", recipe.getDifficulty());
            recipe1.put("category", recipe.getCategory());
            recipe1.put("preparationTime", recipe.getPreparationTime());
            if (userId != null) {
                recipe1.put("userId", userId);
                firestore.collection("recipes_user")
                        .add(recipe1)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Ricetta Utente aggiunta con successo!", Toast.LENGTH_SHORT).show();
                            finish(); // Chiude l'activity
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Errore nell'aggiunta della ricetta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSampleData() {
        // Initialize lists
        savedRecipes = new ArrayList<>();
        globalRecipes = new ArrayList<>();

        // Firebase setup
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    globalRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        globalRecipes.add(recipe);
                    }
                    Log.d("RecipeActivity", "Loaded " + globalRecipes.size() + " recipes from Firestore.");
                    if (tabLayout.getSelectedTabPosition() == 1) {
                        adapter.setRecipes(globalRecipes);
                    }
                })
                .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load recipes: " + e.getMessage()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        firestore.collection("recipes_user")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        savedRecipes.add(recipe);
                    }
                    Log.d("RecipeActivity", "Loaded " + savedRecipes.size() + " recipes from Firestore.");
                    if (tabLayout.getSelectedTabPosition() == 0) {
                        adapter.setRecipes(savedRecipes);
                    }
                })
                .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load recipes: " + e.getMessage()));
    }

    private void setupTabLayout() {
        if (tabLayout == null) {
            Log.e("RecipeActivity", "TabLayout is null. Check R.id.tabLayoutRecipe in recipe.xml.");
            return;
        }

        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Saved Recipes"));
        tabLayout.addTab(tabLayout.newTab().setText("Global Recipes"));

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    adapter.setRecipes(savedRecipes);
                } else if (position == 1) {
                    adapter.setRecipes(globalRecipes);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Default tab
        tabLayout.selectTab(tabLayout.getTabAt(0));
        adapter.setRecipes(savedRecipes);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout == null) {
            Log.e("RecipeActivity", "SwipeRefreshLayout is null. Check R.id.swipeRefreshLayoutRecipe in recipe.xml.");
            return;
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload data
            setupSampleData();
            Toast.makeText(this, "Data updated!", Toast.LENGTH_SHORT).show();

            // Stop refresh animation
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setNavigationListeners() {
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> navigateToActivity(HomeActivity.class));
        } else {
            Log.e("RecipeActivity", "homeButton is null. Check R.id.homeButton in recipe.xml.");
        }

        if (fridgeButton != null) {
            fridgeButton.setOnClickListener(v -> navigateToActivity(FridgeActivity.class));
        } else {
            Log.e("RecipeActivity", "fridgeButton is null. Check R.id.fridgeButton in recipe.xml.");
        }

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> navigateToActivity(SearchActivity.class));
        } else {
            Log.e("RecipeActivity", "searchButton is null. Check R.id.searchButton in recipe.xml.");
        }

        if (addButton != null) {
            addButton.setOnClickListener(v -> startActivity(new Intent(this, AddRecipeActivity.class)));
        } else {
            Log.e("RecipeActivity", "addButton is null. Check R.id.addButtonRecipe in recipe.xml.");
        }
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
