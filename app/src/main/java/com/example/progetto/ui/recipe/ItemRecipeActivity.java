package com.example.progetto.ui.recipe;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ItemRecipeActivity extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView, ingredientsTextView, stepsTextView, difficultyTextView, categoryTextView, preparationTimeTextView;
    private ImageView recipeImageView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_recipe);

        // Trova gli elementi UI
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        ingredientsTextView = findViewById(R.id.ingredientsTextView);
        stepsTextView = findViewById(R.id.stepsTextView);
        difficultyTextView = findViewById(R.id.difficultyTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        preparationTimeTextView = findViewById(R.id.preparationTimeTextView);
        recipeImageView = findViewById(R.id.recipeImageView);

        // Inizializza Firestore
        db = FirebaseFirestore.getInstance();

        // Ottieni l'ID della ricetta dal Intent
        String recipeId = getIntent().getStringExtra("recipeId");

        // Carica i dettagli della ricetta
        db.collection("recipes").document(recipeId).get().addOnSuccessListener(documentSnapshot -> {
            Recipe recipe = documentSnapshot.toObject(Recipe.class);
            if (recipe != null) {
                titleTextView.setText(recipe.getTitle());
                descriptionTextView.setText(recipe.getDescription());
                ingredientsTextView.setText(recipe.getIngredients());
                stepsTextView.setText(recipe.getSteps());
                difficultyTextView.setText(recipe.getDifficulty());
                categoryTextView.setText(recipe.getCategory());
                preparationTimeTextView.setText(recipe.getPreparationTime());
                Glide.with(this).load(recipe.getImageUrl()).into(recipeImageView);
            }
        });
    }
}