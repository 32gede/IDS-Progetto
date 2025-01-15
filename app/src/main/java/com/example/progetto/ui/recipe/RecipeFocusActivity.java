package com.example.progetto.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.adapter.IngredientsAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeFocusActivity extends AppCompatActivity {

    private static final String TAG = "RecipeFocusActivity";

    // UI Components
    private ImageView imageView, backBtn;
    private TextView titleTextView, difficultyTextView, categoryTextView, preparationTimeTextView, stepsTextView, descriptionTextView;
    private RatingBar ratingBar;
    private RecyclerView ingredientsRecyclerView;
    private ImageButton rateButton, editButton;
    private Recipe recipe;

    // Firebase
    private Firestore firestore;
    private FirebaseFirestore databaseReference;

    // Adapters
    private IngredientsAdapter ingredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_focus);

        // Initialize views and Firebase
        initializeViews();
        setupFirestore();
        loadRecipeData();
    }

    // --- INITIALIZATION ---
    // --- INITIALIZATION ---
    private void initializeViews() {
        imageView = findViewById(R.id.recipe_image);
        titleTextView = findViewById(R.id.recipe_title);
        difficultyTextView = findViewById(R.id.recipe_difficulty);
        categoryTextView = findViewById(R.id.recipe_category);
        preparationTimeTextView = findViewById(R.id.recipe_preparation_time);
        stepsTextView = findViewById(R.id.recipe_steps);
        descriptionTextView = findViewById(R.id.recipe_description);
        ratingBar = findViewById(R.id.recipe_rating);
        rateButton = findViewById(R.id.rate_button);
        editButton = findViewById(R.id.edit_button);
        ingredientsRecyclerView = findViewById(R.id.recipe_ingredients_recycler_view);
        backBtn = findViewById(R.id.back_button);

        // Hide edit button by default
        editButton.setVisibility(View.GONE);

        // Setup RecyclerView
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>(), this);
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Back Button
        backBtn.setOnClickListener(v -> finish());
    }

    // --- DATA LOADING ---
    private void loadRecipeData() {
        String recipeId = getIntent().getStringExtra("recipeId");
        firestore.getRecipe(recipeId, new FirestoreCallback<Recipe>() {
            @Override
            public void onSuccess(Recipe appo) {
                recipe = appo;
                displayRecipeDetails();
                setupClickListeners();
                fetchIngredients();
                fetchAndDisplayRatings();
                checkIfRecipeBelongsToUser();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Errore nel recupero della ricetta: " + e.getMessage());
                showToast("Errore nel recupero della ricetta!");
                finish();
            }
        });
    }

    // --- CHECK RECIPE OWNERSHIP ---
    private void checkIfRecipeBelongsToUser() {
        firestore.checkIfRecipe(recipe.getId(), new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean belongsToUser) {
                if (belongsToUser) {
                    editButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Errore nel controllo della proprietà della ricetta: " + e.getMessage());
            }
        });
    }

    private void setupFirestore() {
        firestore = new Firestore();
        databaseReference = FirebaseFirestore.getInstance();
    }

    // --- DISPLAY DATA ---
    private void displayRecipeDetails() {
        titleTextView.setText(recipe.getName());
        descriptionTextView.setText(recipe.getDescription());
        difficultyTextView.setText(recipe.getDifficulty());
        categoryTextView.setText(recipe.getCategory());
        preparationTimeTextView.setText(recipe.getPreparationTime());
        stepsTextView.setText(recipe.getSteps());

        Glide.with(this)
                .load(recipe.getImage())
                .error(R.drawable.baseline_error_24)
                .into(imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ricarica la ricetta e aggiorna la UI
        reloadPage();
    }

    private void updateRatingBar(float averageRating) {
        ratingBar.setRating(averageRating);
        ratingBar.setIsIndicator(true);
    }

    // --- CLICK LISTENERS ---
    private void setupClickListeners() {
        rateButton.setOnClickListener(v -> showRatingDialog());
        editButton.setOnClickListener(v -> openEditRecipeActivity());
    }

    // --- FETCH DATA ---
    private void fetchIngredients() {
        firestore.getSelectIngredients(recipe.getId(), new FirestoreCallback<List<SelectedIngredientUtils>>() {
            @Override
            public void onSuccess(List<SelectedIngredientUtils> ingredients) {
                ingredientsAdapter.updateData(ingredients);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Errore nel recupero degli ingredienti: " + e.getMessage());
            }
        });
    }

    private void fetchAndDisplayRatings() {
        databaseReference.collection("ratings")
                .whereEqualTo("recipeId", recipe.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Float> ratings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ratings.add(document.getDouble("rating").floatValue());
                    }
                    if (!ratings.isEmpty()) {
                        float averageRating = calculateAverage(ratings);
                        updateRatingBar(averageRating);
                        updateRatingInFirestore(averageRating);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel recupero dei rating: " + e.getMessage()));
    }

    private float calculateAverage(List<Float> ratings) {
        float sum = 0;
        for (float rating : ratings) sum += rating;
        return sum / ratings.size();
    }

    private void updateRatingInFirestore(float averageRating) {
        databaseReference.collection("recipes").document(recipe.getId())
                .update("averageRating", averageRating)
                .addOnFailureListener(e -> Log.e(TAG, "Errore nell'aggiornamento del rating medio: " + e.getMessage()));
    }

    // --- INTERACTIONS ---
    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Valuta la ricetta");

        // Setup Rating Layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final RatingBar dialogRatingBar = new RatingBar(this);
        dialogRatingBar.setNumStars(5);
        dialogRatingBar.setStepSize(1.0f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        dialogRatingBar.setLayoutParams(params);

        layout.addView(dialogRatingBar);
        builder.setView(layout);

        builder.setPositiveButton("Invia", (dialog, which) -> {
            float rating = dialogRatingBar.getRating();
            saveRatingAndReloadPage(rating);
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void saveRatingAndReloadPage(float rating) {
        Map<String, Object> ratingMap = Map.of(
                "recipeId", recipe.getId(),
                "rating", rating
        );
        firestore.addSomething(ratingMap, "ratings");

        databaseReference.collection("ratings")
                .whereEqualTo("recipeId", recipe.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Float> ratings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ratings.add(document.getDouble("rating").floatValue());
                    }
                    if (!ratings.isEmpty()) {
                        float newAverageRating = calculateAverage(ratings);
                        updateRecipeRatingAndReload(newAverageRating);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel recupero delle valutazioni: " + e.getMessage()));
    }

    private void updateRecipeRatingAndReload(float newAverageRating) {
        databaseReference.collection("recipes").document(recipe.getId())
                .update("averageRating", newAverageRating)
                .addOnSuccessListener(unused -> {
                    recipe.setAverageRating(newAverageRating);
                    reloadPage();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nell'aggiornamento del rating: " + e.getMessage()));
    }

    private void reloadPage() {
        loadRecipeData();
        showToast("Pagina aggiornata con successo!");
    }

    private void openEditRecipeActivity() {
        Intent intent = new Intent(this, RecipeEditActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }

    // --- UTILITY ---
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
