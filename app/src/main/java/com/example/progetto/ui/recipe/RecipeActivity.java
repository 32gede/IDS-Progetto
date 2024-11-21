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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.UserRecipeUtils;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;


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
    private List<Recipe> globalRecipes;
    private List<UserRecipeUtils> savedRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Initialize UI components
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data and configure tabs
        loadRecipesFromFirestore();
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

        ImageButton profileButtonTop = findViewById(R.id.profileButtonTop);
        if (profileButtonTop != null) {
            profileButtonTop.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    // src/main/java/com/example/progetto/ui/recipe/RecipeActivity.java
// Update the setupRecyclerView method
    // src/main/java/com/example/progetto/ui/recipe/RecipeActivity.java
// Update the setupRecyclerView method
    private void setupRecyclerView() {
        if (recyclerView == null) {
            Log.e("RecipeActivity", "RecyclerView is null. Check R.id.recyclerViewRecipes in recipe.xml.");
            return;
        }
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
        }, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadRecipesFromFirestore() {
        globalRecipes = new ArrayList<>();
        savedRecipes = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Carica le ricette globali
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    globalRecipes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);

                        // Imposta l'ID del documento come l'ID della ricetta
                        recipe.setId(document.getId());

                        globalRecipes.add(recipe);
                    }
                    if (tabLayout.getSelectedTabPosition() == 1) {
                        adapter.setRecipes(globalRecipes);
                    }
                })
                .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load global recipes: " + e.getMessage()));

        // Carica le ricette salvate dall'utente
        if (userId != null) {
            firestore.collection("recipes_user")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        savedRecipes.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            UserRecipeUtils userRecipe = document.toObject(UserRecipeUtils.class);

                            // Imposta l'ID del documento come l'ID della ricetta
                            userRecipe.setId(document.getId());

                            savedRecipes.add(userRecipe);
                        }
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            adapter.setRecipes(new ArrayList<>(savedRecipes));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("RecipeActivity", "Failed to load user recipes: " + e.getMessage()));
        }
    }


    private void saveRecipeToUserCollection(Recipe recipe) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (recipe == null || recipe.getId() == null) {
            Log.e("RecipeActivity", "Recipe o ID della ricetta è nullo. Salvataggio annullato.");
            Toast.makeText(this, "Errore: la ricetta non è valida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId != null) {
            UserRecipeUtils userRecipe = new UserRecipeUtils(recipe, userId);
            firestore.collection("recipes_user")
                    .document(recipe.getId()) // Usa l'ID della ricetta come nome del documento
                    .set(userRecipe)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ricetta salvata con successo!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Errore nel salvataggio: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Utente non autenticato!", Toast.LENGTH_SHORT).show();
        }
    }


    private void removeRecipeFromUserCollection(Recipe recipe) {
        firestore.collection("recipes_user")
                .document(recipe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ricetta rimossa con successo!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Errore nella rimozione: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupTabLayout() {
    tabLayout.addTab(tabLayout.newTab().setText("Saved Recipes"));
    tabLayout.addTab(tabLayout.newTab().setText("Global Recipes"));

    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(@NonNull TabLayout.Tab tab) {
            loadRecipesFromFirestore(); // Reload recipes from Firestore

            Set<String> savedRecipeIds = new HashSet<>();
            for (UserRecipeUtils userRecipe : savedRecipes) {
                savedRecipeIds.add(userRecipe.getId());
            }
            adapter.setSavedRecipeIds(savedRecipeIds);

            if (tab.getPosition() == 0) {
                adapter.setRecipes(new ArrayList<>(savedRecipes));
            } else {
                adapter.setRecipes(globalRecipes);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}

        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    });

    tabLayout.selectTab(tabLayout.getTabAt(0));
    Set<String> savedRecipeIds = new HashSet<>();
    for (UserRecipeUtils userRecipe : savedRecipes) {
        savedRecipeIds.add(userRecipe.getId());
    }
    adapter.setSavedRecipeIds(savedRecipeIds);
    adapter.setRecipes(new ArrayList<>(savedRecipes));
}

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRecipesFromFirestore();
            swipeRefreshLayout.setRefreshing(false);
        });
    }


    private void setNavigationListeners() {
        homeButton.setOnClickListener(v -> navigateToActivity(HomeActivity.class));
        fridgeButton.setOnClickListener(v -> navigateToActivity(FridgeActivity.class));
        searchButton.setOnClickListener(v -> navigateToActivity(SearchActivity.class));
        addButton.setOnClickListener(v -> startActivity(new Intent(this, AddRecipeActivity.class)));
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}