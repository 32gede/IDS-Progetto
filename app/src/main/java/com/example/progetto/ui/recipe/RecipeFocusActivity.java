package com.example.progetto.ui.recipe;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

    private ImageView imageView;
    private TextView titleTextView, difficultyTextView, categoryTextView, preparationTimeTextView, stepsTextView, descriptionTextView;
    private RatingBar ratingBar;
    private RecyclerView ingredientsRecyclerView;
    private ImageButton rateButton;
    private FirebaseFirestore databaseReference;
    private IngredientsAdapter ingredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_focus);

        // Inizializza le viste
        initializeViews();

        // Inizializza Firebase
        databaseReference = FirebaseFirestore.getInstance();

        // Recupera la ricetta passata dall'activity precedente
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        // Mostra i dettagli della ricetta
        if (recipe != null) {
            displayRecipeDetails(recipe);
            fetchAndDisplayRating(recipe.getId());
        }

        // Imposta il listener per il pulsante di valutazione
        rateButton.setOnClickListener(v -> showRatingDialog(recipe));
        getIngredients(recipe.getId());
    }

    private void initializeViews() {
        imageView = findViewById(R.id.recipe_image);
        titleTextView = findViewById(R.id.recipe_title);
        difficultyTextView = findViewById(R.id.recipe_difficulty);
        categoryTextView = findViewById(R.id.recipe_category);
        preparationTimeTextView = findViewById(R.id.recipe_preparation_time);
        ingredientsRecyclerView = findViewById(R.id.recipe_ingredients_recycler_view);
        stepsTextView = findViewById(R.id.recipe_steps);
        descriptionTextView = findViewById(R.id.recipe_description);
        ratingBar = findViewById(R.id.recipe_rating);
        rateButton = findViewById(R.id.rate_button);

        // Initialize the adapter
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>(), this);
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);

        // Set a layout manager
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void displayRecipeDetails(Recipe recipe) {
        Log.d(TAG, "Displaying recipe details for: " + recipe.getName());
        titleTextView.setText(recipe.getName());
        descriptionTextView.setText(recipe.getDescription());
        difficultyTextView.setText(recipe.getDifficulty());
        categoryTextView.setText(recipe.getCategory());
        preparationTimeTextView.setText(recipe.getPreparationTime());
        stepsTextView.setText(recipe.getSteps());

        // Carica l'immagine utilizzando Glide
        Glide.with(this)
                .load(recipe.getImage())
                .error(R.drawable.baseline_error_24)
                .into(imageView);
    }

    private void getIngredients(String recipeId) {
        Log.d(TAG, "Fetching ingredients for recipe ID: " + recipeId);
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
                    // Update adapter data
                    ingredientsAdapter.updateData(ingredients);
                    Log.d(TAG, "Ingredients loaded: " + ingredients.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch ingredients: " + e.getMessage());
                    Toast.makeText(this, "Failed to fetch ingredients", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAndDisplayRating(String recipeId) {
        Log.d(TAG, "Fetching ratings for recipe ID: " + recipeId);
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
                        calculateAverageRating(recipeId, ratings);
                    }
                    Log.d(TAG, "Ratings loaded: " + ratings.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch ratings: " + e.getMessage());
                    Toast.makeText(RecipeFocusActivity.this, "Failed to fetch ratings", Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateAverageRating(String recipeId, List<Float> ratings) {
        Log.d(TAG, "Calculating average rating for recipe ID: " + recipeId);
        float sum = 0;
        for (float rating : ratings) {
            sum += rating;
        }
        float averageRating = sum / ratings.size();

        // Update the average rating in the Recipe document
        databaseReference.collection("recipes").document(recipeId)
                .update("averageRating", averageRating)
                .addOnSuccessListener(aVoid -> {
                    ratingBar.setRating(averageRating);
                    ratingBar.setIsIndicator(true); // Make the RatingBar non-interactive
                    Log.d(TAG, "Average rating updated: " + averageRating);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update average rating: " + e.getMessage());
                    Toast.makeText(RecipeFocusActivity.this, "Failed to update average rating", Toast.LENGTH_SHORT).show();
                });
    }

    private void showRatingDialog(Recipe recipe) {
        Log.d(TAG, "Showing rating dialog for recipe: " + recipe.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate this recipe");

        // Configura il RatingBar con un LinearLayout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setMax(5);
        ratingBar.setStepSize(1);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        ratingBar.setLayoutParams(params);

        layout.addView(ratingBar);
        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = ratingBar.getRating();
            saveRatingToFirebase(recipe.getId(), rating);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveRatingToFirebase(String recipeId, float rating) {
        Log.d(TAG, "Saving rating to Firebase for recipe ID: " + recipeId);
        String ratingId = databaseReference.collection("ratings").document().getId();
        Rating ratingObj = new Rating(recipeId, rating);

        databaseReference.collection("ratings").document(ratingId).set(ratingObj)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Rating submitted successfully");
                    Toast.makeText(RecipeFocusActivity.this, "Rating submitted", Toast.LENGTH_SHORT).show();
                    recreate(); // Refresh the activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to submit rating: " + e.getMessage());
                    Toast.makeText(RecipeFocusActivity.this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                });
    }

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