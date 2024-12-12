package com.example.progetto.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeFocusActivity extends AppCompatActivity {

    private static final String TAG = "RecipeFocusActivity";

    // UI Components
    private ImageView imageView;
    private TextView titleTextView, difficultyTextView, categoryTextView, preparationTimeTextView, stepsTextView, descriptionTextView;
    private RatingBar ratingBar;
    private RecyclerView ingredientsRecyclerView;
    private ImageButton rateButton, editButton;

    // Firebase
    private FirebaseFirestore databaseReference;

    // Adapters
    private IngredientsAdapter ingredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_focus);

        // Initialize views and Firebase
        initializeViews();
        databaseReference = FirebaseFirestore.getInstance();

        // Retrieve recipe data
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (recipe == null) {
            Toast.makeText(this, "Errore: ricetta non trovata!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display recipe details
        displayRecipeDetails(recipe);

        // Set click listeners
        setupClickListeners(recipe);

        // Fetch additional data (ingredients and ratings)
        fetchIngredients(recipe.getId());
        fetchAndDisplayRatings(recipe.getId());
    }

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

        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>(), this);
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners(@NonNull Recipe recipe) {
        rateButton.setOnClickListener(v -> showRatingDialog(recipe));
        editButton.setOnClickListener(v -> openEditRecipeActivity(recipe));
    }

    // --- DISPLAY DATA ---

    private void displayRecipeDetails(@NonNull Recipe recipe) {
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

    private void updateRatingBar(float averageRating) {
        ratingBar.setRating(averageRating);
        ratingBar.setIsIndicator(true);
    }

    // --- FETCH DATA ---

    private void fetchIngredients(String recipeId) {
        databaseReference.collection("SelectedIngredient")
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SelectedIngredientUtils> ingredients = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SelectedIngredientUtils item = document.toObject(SelectedIngredientUtils.class);
                        if (item != null) {
                            ingredients.add(item);
                        }
                    }
                    ingredientsAdapter.updateData(ingredients);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel recupero degli ingredienti: " + e.getMessage()));
    }

    private void fetchAndDisplayRatings(String recipeId) {
        databaseReference.collection("ratings")
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Float> ratings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Rating rating = document.toObject(Rating.class);
                        ratings.add(rating.rating);
                    }
                    if (!ratings.isEmpty()) {
                        calculateAndDisplayAverageRating(recipeId, ratings);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel recupero dei rating: " + e.getMessage()));
    }

    private void calculateAndDisplayAverageRating(String recipeId, List<Float> ratings) {
        float sum = 0;
        for (float rating : ratings) {
            sum += rating;
        }
        float averageRating = sum / ratings.size();
        updateRatingInFirestore(recipeId, averageRating);
        updateRatingBar(averageRating);
    }

    private void updateRatingInFirestore(String recipeId, float averageRating) {
        databaseReference.collection("recipes").document(recipeId)
                .update("averageRating", averageRating)
                .addOnFailureListener(e -> Log.e(TAG, "Errore nell'aggiornamento del rating medio: " + e.getMessage()));
    }

    // --- INTERACTIONS ---

    private void showRatingDialog(@NonNull Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Valuta la ricetta");

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
            saveRatingToFirestore(recipe.getId(), rating);
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void saveRatingToFirestore(String recipeId, float rating) {
        String ratingId = databaseReference.collection("ratings").document().getId();
        Rating ratingObj = new Rating(recipeId, rating);

        databaseReference.collection("ratings").document(ratingId)
                .set(ratingObj)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Rating inviato!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e(TAG, "Errore nell'invio del rating: " + e.getMessage()));
    }

    private void openEditRecipeActivity(@NonNull Recipe recipe) {
        Intent intent = new Intent(this, RecipeEditActivity.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }

    // --- INNER CLASSES ---

    public static class Rating {
        public String recipeId;
        public float rating;

        public Rating() {
        }

        public Rating(String recipeId, float rating) {
            this.recipeId = recipeId;
            this.rating = rating;
        }
    }
}
