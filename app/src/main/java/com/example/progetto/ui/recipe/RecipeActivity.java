package com.example.progetto.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.UserProductUtils;
import com.example.progetto.data.model.UserRecipeUtils;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.data.model.BottomNavigationHelper;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeActivity extends AppCompatActivity {

    // UI Components
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Firebase and Adapter
    private FirebaseFirestore firestore;
    private RecipeAdapter adapter;

    // Recipe Data
    private List<Recipe> globalRecipes;
    private List<UserRecipeUtils> savedRecipes;
    private List<Recipe> cookedRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.recipe_button);
            BottomNavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data and configure tabs
        loadRecipesFromFirestore();
        setupTabLayout();

        // Setup SwipeRefreshLayout
        setupSwipeRefresh();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayoutRecipe);
        recyclerView = findViewById(R.id.recyclerViewRecipes);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutRecipe);
        addButton = findViewById(R.id.addButtonRecipe);

        ImageButton profileButtonTop = findViewById(R.id.profileButtonTop);
        if (profileButtonTop != null) {
            profileButtonTop.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        addButton.setOnClickListener(v -> startActivity(new Intent(this, AddRecipeActivity.class)));
    }

    private void setupRecyclerView() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecipeAdapter(new ArrayList<>(), (recipe, isSaved) -> {
            if (isSaved) {
                saveRecipeToUserCollection(recipe);
            } else {
                removeRecipeFromUserCollection(recipe);
            }
        }, this, false);

        recyclerView.setAdapter(adapter);
    }

    private void loadRecipesFromFirestore() {
        globalRecipes = new ArrayList<>();
        savedRecipes = new ArrayList<>();
        cookedRecipes = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        Set<String> savedRecipeIds = new HashSet<>();

        // Load global recipes
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    globalRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setId(document.getId());
                        globalRecipes.add(recipe);
                    }
                    if (tabLayout.getSelectedTabPosition() == 1) {
                        adapter.setRecipes(globalRecipes);
                    }
                })
                .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load global recipes: " + e.getMessage()));

        // Load user-saved recipes
        if (userId != null) {
            firestore.collection("recipes_user")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        savedRecipes.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            UserRecipeUtils userRecipe = document.toObject(UserRecipeUtils.class);
                            userRecipe.setId(document.getId());
                            savedRecipes.add(userRecipe);
                            savedRecipeIds.add(userRecipe.getId());
                        }

                        adapter.setSavedRecipeIds(savedRecipeIds);
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            adapter.setRecipes(new ArrayList<>(savedRecipes));
                        }

                        loadCookedRecipes(userId);
                    })
                    .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load user recipes: " + e.getMessage()));
        }
    }

    private void loadCookedRecipes(String userId) {
        Set<String> userIngredients = new HashSet<>();
        firestore.collection("user_products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userIngredients.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productName = document.getString("name");
                        if (productName != null) {
                            userIngredients.add(productName);
                        }
                    }

                    cookedRecipes.clear();
                    for (Recipe recipe : globalRecipes) {
                        List<String> recipeIngredients = Arrays.asList(recipe.getIngredients().split(","));
                        if (userIngredients.containsAll(recipeIngredients)) {
                            cookedRecipes.add(recipe);
                        }
                    }
                    if (tabLayout.getSelectedTabPosition() == 2) {
                        adapter.setRecipes(cookedRecipes);
                    }
                })
                .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load user products: " + e.getMessage()));
    }

    private void saveRecipeToUserCollection(Recipe recipe) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null && recipe != null && recipe.getId() != null) {
            UserRecipeUtils userRecipe = new UserRecipeUtils(recipe, userId);
            firestore.collection("recipes_user")
                    .document(recipe.getId())
                    .set(userRecipe)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ricetta salvata con successo!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e("RecipeActivity", "Errore nel salvataggio: " + e.getMessage()));
        }
    }

    private void removeRecipeFromUserCollection(Recipe recipe) {
        firestore.collection("recipes_user")
                .document(recipe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ricetta rimossa con successo!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e("RecipeActivity", "Errore nella rimozione: " + e.getMessage()));
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Saved Recipes"));
        tabLayout.addTab(tabLayout.newTab().setText("Global Recipes"));
        tabLayout.addTab(tabLayout.newTab().setText("Cooked Recipes"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.setRecipes(new ArrayList<>(savedRecipes));
                } else if (tab.getPosition() == 1) {
                    adapter.setRecipes(globalRecipes);
                } else {
                    adapter.setRecipes(cookedRecipes);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRecipesFromFirestore();
            swipeRefreshLayout.setRefreshing(false);
        });
    }
}
