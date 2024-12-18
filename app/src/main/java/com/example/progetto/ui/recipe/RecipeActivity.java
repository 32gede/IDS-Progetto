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
    Set<String> savedRecipeIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);
        firestore = new Firestore();

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
        // UI Components
        FloatingActionButton addButton = findViewById(R.id.addButtonRecipe);
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
        savedRecipeIds = new HashSet<>();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        loadGlobalRecipes();
        loadSavedRecipes(userId);
        loadCookedRecipes(userId);
    }

    public void loadGlobalRecipes() {
        Log.d(TAG, "Loading global recipes");
        firestore.loadGlobalRecipes(new FirestoreCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                globalRecipes.addAll(recipes);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("RecipeActivity", "Failed to load global recipes: " + e.getMessage());
            }
        });
    }

    private void loadSavedRecipes(String uid) {
        Log.d(TAG, "Loading user recipes for user: " + uid);
        firestore.loadUserRecipes(uid, new FirestoreCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                savedRecipes.clear();
                savedRecipes.addAll(recipes);
                for (Recipe userRecipe : savedRecipes) {
                    savedRecipeIds.add(userRecipe.getId());
                }
                adapter.setSavedRecipeIds(savedRecipeIds);
                Log.d(TAG, "Saved recipes loaded: " + savedRecipes.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("RecipeActivity", "Failed to load user recipes: " + e.getMessage());
            }
        });
    }

    private void loadCookedRecipes(String userId) {
        Log.d(TAG, "Filtering cookable recipes for user: " + userId);

        firestore.loadCookableRecipes(userId, new FirestoreCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> cookableRecipes) {
                // Stampa tutte le ricette cookable
                cookableRecipe.addAll(cookableRecipes);
                Log.d(TAG, "Cookable recipes loaded: " + cookableRecipe.size());
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("Errore durante il caricamento delle ricette cookable: " + e.getMessage());
            }
        });
    }

    private void saveRecipeToUserCollection(Recipe recipe) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null && recipe != null && recipe.getId() != null) {
            UserRecipeUtils userRecipe = new UserRecipeUtils(userId, recipe.getId());
            firestore.addSomething(userRecipe.toMap(), "recipes_user");
        }
    }

    private void removeRecipeFromUserCollection(Recipe recipe) {
        firestore.removeUserRecipe(recipe, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(RecipeActivity.this, "Ricetta rimossa con successo!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("RecipeActivity", "Errore nella rimozione: " + e.getMessage());
            }
        });
    }

    private void setupTabLayout() {

        tabLayout.addTab(tabLayout.newTab().setText(R.string.saved_recipe));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.global_recipe));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.recommended_recipe));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.setRecipes(savedRecipes);
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
