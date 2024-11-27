package com.example.progetto.ui.recipe;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RecipeFocusActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView titleTextView, difficultyTextView, categoryTextView, preparationTimeTextView, ingredientsTextView, stepsTextView, descriptionTextView;
    private RatingBar ratingBar;
    private ImageButton rateButton;
    private FirebaseFirestore databaseReference;

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
    }

    private void initializeViews() {
        imageView = findViewById(R.id.recipe_image);
        titleTextView = findViewById(R.id.recipe_title);
        difficultyTextView = findViewById(R.id.recipe_difficulty);
        categoryTextView = findViewById(R.id.recipe_category);
        preparationTimeTextView = findViewById(R.id.recipe_preparation_time);
        ingredientsTextView = findViewById(R.id.recipe_ingredients);
        stepsTextView = findViewById(R.id.recipe_steps);
        descriptionTextView = findViewById(R.id.recipe_description);
        ratingBar = findViewById(R.id.recipe_rating);
        rateButton = findViewById(R.id.rate_button);
    }

    private void displayRecipeDetails(Recipe recipe) {
        titleTextView.setText(recipe.getName());
        descriptionTextView.setText(recipe.getDescription());
        difficultyTextView.setText(recipe.getDifficulty());
        categoryTextView.setText(recipe.getCategory());
        preparationTimeTextView.setText(recipe.getPreparationTime());
        ingredientsTextView.setText(recipe.getIngredients());
        stepsTextView.setText(recipe.getSteps());

        // Carica l'immagine utilizzando Glide
        Glide.with(this)
                .load(recipe.getImage())
                .error(R.drawable.baseline_error_24)
                .into(imageView);
    }

    private void fetchAndDisplayRating(String recipeId) {
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
                })
                .addOnFailureListener(e -> Toast.makeText(RecipeFocusActivity.this, "Failed to fetch ratings", Toast.LENGTH_SHORT).show());
    }

    private void calculateAverageRating(String recipeId, List<Float> ratings) {
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
                })
                .addOnFailureListener(e -> Toast.makeText(RecipeFocusActivity.this, "Failed to update average rating", Toast.LENGTH_SHORT).show());
    }

    private void showRatingDialog(Recipe recipe) {
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
        String ratingId = databaseReference.collection("ratings").document().getId();
        Rating ratingObj = new Rating(recipeId, rating);

        databaseReference.collection("ratings").document(ratingId).set(ratingObj)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RecipeFocusActivity.this, "Rating submitted", Toast.LENGTH_SHORT).show();
                    recreate(); // Refresh the activity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(RecipeFocusActivity.this, "Failed to submit rating", Toast.LENGTH_SHORT).show()
                );
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