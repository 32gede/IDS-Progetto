package com.example.progetto.ui.recipe;

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
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.UserRecipeUtils;
import com.example.progetto.data.model.NavigationHelper;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";

    // UI Components
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth auth;

    // Firebase and Adapter
    private Firestore firestore;
    private RecipeAdapter adapter;

    // Recipe Data
    private List<Recipe> globalRecipes;
    private List<Recipe> savedRecipes;
    private List<Recipe> cookableRecipe;
    private Set<String> savedRecipeIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        auth = FirebaseAuth.getInstance();
        firestore = new Firestore();

        initializeUI();
        setupRecyclerView();
        setupTabLayout();
        setupSwipeRefresh();

        loadRecipesFromFirestore();
    }

    // ======== Initialization Methods ========

    /**
     * Initialize UI components.
     */
    private void initializeUI() {
        // Toolbar and navigation setup
        NavigationHelper.setupToolbar(
                findViewById(R.id.profileButton),
                findViewById(R.id.notificationButton),
                this
        );

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.recipe_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Find views
        tabLayout = findViewById(R.id.tabLayoutRecipe);
        recyclerView = findViewById(R.id.recyclerViewRecipes);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutRecipe);

        FloatingActionButton addButton = findViewById(R.id.addButtonRecipe);
        addButton.setOnClickListener(v -> startActivity(new Intent(this, AddRecipeActivity.class)));
    }

    /**
     * Setup RecyclerView with Flexbox layout manager and RecipeAdapter.
     */
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

    /**
     * Setup TabLayout for navigating between different recipe lists.
     */
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.saved_recipe));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.global_recipe));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.recommended_recipe));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        adapter.setRecipes(savedRecipes);
                        break;
                    case 1:
                        adapter.setRecipes(globalRecipes);
                        break;
                    case 2:
                        adapter.setRecipes(cookableRecipe);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    /**
     * Setup SwipeRefreshLayout for refreshing recipe data.
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRecipesFromFirestore();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    // ======== Recipe Loading Methods ========

    /**
     * Load all recipe lists from Firestore.
     */
    private void loadRecipesFromFirestore() {
        globalRecipes = new ArrayList<>();
        savedRecipes = new ArrayList<>();
        cookableRecipe = new ArrayList<>();
        savedRecipeIds = new HashSet<>();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        loadSavedRecipes(userId);loadGlobalRecipes();
        loadCookedRecipes(userId);
    }

    /**
     * Load global recipes from Firestore.
     */
    private void loadGlobalRecipes() {
        Log.d(TAG, "Loading global recipes");
        firestore.loadGlobalRecipes(new FirestoreCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                globalRecipes.addAll(recipes);
                Log.d(TAG, "Global recipes loaded: " + globalRecipes.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load global recipes: " + e.getMessage());
            }
        });
    }

    /**
     * Load user-saved recipes from Firestore.
     */
    private void loadSavedRecipes(String userId) {
        Log.d(TAG, "Loading user recipes for user: " + userId);
        firestore.loadUserRecipes(userId, new FirestoreCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                savedRecipes.clear();
                savedRecipes.addAll(recipes);
                for (Recipe userRecipe : savedRecipes) {
                    if (userRecipe != null && userRecipe.getId() != null) {
                        savedRecipeIds.add(userRecipe.getId());
                    }
                }
                adapter.setSavedRecipeIds(savedRecipeIds);
                Log.d(TAG, "Saved recipes loaded: " + savedRecipes.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load user recipes: " + e.getMessage());
            }
        });
    }

    /**
     * Load cookable recipes based on user's saved ingredients.
     */
    private void loadCookedRecipes(String userId) {
        Log.d(TAG, "Filtering cookable recipes for user: " + userId);
        firestore.loadCookableRecipes(userId, new FirestoreCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> cookableRecipes) {
                cookableRecipe.addAll(cookableRecipes);
                Log.d(TAG, "Cookable recipes loaded: " + cookableRecipe.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load cookable recipes: " + e.getMessage());
            }
        });
    }

    // ======== Recipe Collection Management ========

    /**
     * Save a recipe to the user's Firestore collection.
     */
    private void saveRecipeToUserCollection(Recipe recipe) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId != null && recipe != null && recipe.getId() != null) {
            UserRecipeUtils userRecipe = new UserRecipeUtils(userId, recipe.getId());
            firestore.addSomething(userRecipe.toMap(), "recipes_user");
        }
    }

    /**
     * Remove a recipe from the user's Firestore collection.
     */
    private void removeRecipeFromUserCollection(Recipe recipe) {
        firestore.removeUserRecipe(recipe,auth.getCurrentUser().getUid(), new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(RecipeActivity.this, "Ricetta rimossa con successo!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Errore nella rimozione: " + e.getMessage());
            }
        });
    }
}
