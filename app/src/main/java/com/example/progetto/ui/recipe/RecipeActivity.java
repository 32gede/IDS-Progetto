package com.example.progetto.ui.recipe;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.SelectedIngredientRecipeUtils;
import com.example.progetto.data.model.UserRecipeUtils;
import com.example.progetto.data.model.NavigationHelper;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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
    private List<Recipe> cookableRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.recipe_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
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
        NavigationHelper.setupToolbar(findViewById(R.id.profileButton), findViewById(R.id.notificationButton), this);
        tabLayout = findViewById(R.id.tabLayoutRecipe);
        recyclerView = findViewById(R.id.recyclerViewRecipes);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutRecipe);
        addButton = findViewById(R.id.addButtonRecipe);
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
        cookableRecipe = new ArrayList<>();
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

                            if (canCook && cookableRecipe.size() < 5) {
                                cookableRecipe.add(recipe);
                            }
                        }
                        adapter.notifyDataSetChanged();
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
                    adapter.setRecipes(cookableRecipe);
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
