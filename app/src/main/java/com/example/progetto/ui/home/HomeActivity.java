package com.example.progetto.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.NavigationHelper;
import com.example.progetto.data.model.SelectedIngredientRecipeUtils;
import com.example.progetto.ui.profile.ProfileActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // UI Components
    private RecyclerView popularRecyclerView;
    private RecyclerView newerRecyclerView;
    private RecyclerView cookableRecyclerView;
    private RecipeAdapter adapterPopular;
    private RecipeAdapter adapterNewer;
    private RecipeAdapter adapterCookable;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView titleText;

    // Firebase
    private FirebaseFirestore firestore;

    // Data
    private final List<Recipe> popularRecipe = new ArrayList<>();
    private final List<Recipe> newerRecipe = new ArrayList<>();
    private final List<Recipe> cookableRecipe = new ArrayList<>();
    private final List<Recipe> globalRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.home_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        initializeViews();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load Recipes
        loadRecipes();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.title);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        titleText.setText(getString(R.string.home));

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh triggered");
            loadRecipes();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecyclerViews() {
        popularRecyclerView = findViewById(R.id.popularRecyclerView);
        newerRecyclerView = findViewById(R.id.newerRecipeRecyclerView);
        cookableRecyclerView = findViewById(R.id.cookableRecyclerView);

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
        firestore.collection("recipes")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    popularRecipe.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            popularRecipe.add(recipe);
                        }
                    }
                    adapterPopular.notifyDataSetChanged();
                    Log.d(TAG, "Top 5 popular recipes loaded successfully.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading popular recipes: " + e.getMessage()));
    }

    private void loadNewerRecipes() {
        Log.d(TAG, "Loading newer recipes");
        firestore.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newerRecipe.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            newerRecipe.add(recipe);
                        }
                    }
                    adapterNewer.notifyDataSetChanged();
                    Log.d(TAG, "Latest 5 recipes loaded successfully.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading newer recipes: " + e.getMessage()));
    }

    private void loadCookableRecipes(String userId) {
        if (userId == null) {
            Log.e(TAG, "User ID is null, cannot load cookable recipes.");
            return;
        }

        Log.d(TAG, "Loading global recipes");
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    globalRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        if (recipe != null) {
                            globalRecipes.add(recipe);
                        }
                    }
                    Log.d(TAG, "Global recipes loaded: " + globalRecipes.size());
                    filterCookableRecipes(userId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading global recipes: " + e.getMessage()));
    }

    private void filterCookableRecipes(String userId) {
        Log.d(TAG, "Filtering cookable recipes for user: " + userId);
        ritorna().addOnSuccessListener(recipeIngredients -> {
            firestore.collection("user_products")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Set<String> userIngredients = new HashSet<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String productName = document.getString("name");
                            if (productName != null) {
                                userIngredients.add(productName);
                            }
                        }

                        cookableRecipe.clear();
                        for (Recipe recipe : globalRecipes) {
                            Log.d(TAG, "Checking recipe: " + recipe.getName());
                            boolean canCook = true;

                            for (SelectedIngredientRecipeUtils ingredient : recipeIngredients) {
                                if (ingredient.getRecipeId().equals(recipe.getId())) {
                                    Log.d(TAG, "Ingredient: " + ingredient.getName() + " required quantity: " + ingredient.getQuantity());

                                    boolean hasIngredient = false;
                                    for (QueryDocumentSnapshot userIngredientDoc : queryDocumentSnapshots) {
                                        String userIngredientName = userIngredientDoc.getString("name");
                                        Long userIngredientQuantity = userIngredientDoc.getLong("quantity");

                                        if (userIngredientName != null && userIngredientName.equals(ingredient.getName())) {
                                            Log.d(TAG, "User has ingredient: " + userIngredientName + " with quantity: " + userIngredientQuantity);

                                            // Check if the user has enough quantity
                                            if (userIngredientQuantity != null && userIngredientQuantity >= ingredient.getQuantity()) {
                                                hasIngredient = true;
                                            }
                                            break; // No need to check further if ingredient matches
                                        }
                                    }

                                    if (!hasIngredient) {
                                        canCook = false;
                                        break;
                                    }
                                }
                            }

                            if (canCook&&cookableRecipe.size()<5) {
                                cookableRecipe.add(recipe);
                            }
                        }
                        adapterCookable.notifyDataSetChanged();
                        Log.d(TAG, "Cookable recipes loaded: " + cookableRecipe.size());
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user ingredients: " + e.getMessage()));
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading selected ingredients: " + e.getMessage()));
    }

    public Task<List<SelectedIngredientRecipeUtils>> ritorna() {
        Log.d(TAG, "Loading selected ingredients");
        return firestore.collection("SelectedIngredient")
                .whereEqualTo("position", 1)
                .get()
                .continueWith(task -> {
                    List<SelectedIngredientRecipeUtils> selectedIngredients = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SelectedIngredientRecipeUtils ingredient = document.toObject(SelectedIngredientRecipeUtils.class);
                            if (ingredient != null) {
                                selectedIngredients.add(ingredient);
                            }
                        }
                    }

                    Log.d(TAG, "Selected ingredients loaded: " + selectedIngredients.size());
                    return selectedIngredients;
                });
    }
}