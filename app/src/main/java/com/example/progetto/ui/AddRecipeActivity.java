package com.example.progetto.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText recipeName, recipeDescription, recipeIngredients, recipeSteps, recipeImage, recipeDifficulty, recipeCategory, recipePreparationTime;
    private Button btnSubmitRecipe;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Inizializza Firestore
        db = FirebaseFirestore.getInstance();

        // Trova gli elementi UI
        recipeName = findViewById(R.id.recipe_name);
        recipeDescription = findViewById(R.id.recipe_description);
        recipeIngredients = findViewById(R.id.recipe_ingredients);
        recipeSteps = findViewById(R.id.recipe_steps);
        recipeImage = findViewById(R.id.recipe_image);
        recipeDifficulty = findViewById(R.id.recipe_difficulty);
        recipeCategory = findViewById(R.id.recipe_category);
        recipePreparationTime = findViewById(R.id.recipe_preparation_time);
        btnSubmitRecipe = findViewById(R.id.btn_submit_recipe);

        // Listener per salvare la ricetta
        btnSubmitRecipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                saveRecipe();
            }
        });
    }

    private void saveRecipe() {
        String name = recipeName.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();
        String ingredients = recipeIngredients.getText().toString().trim();
        String steps = recipeSteps.getText().toString().trim();
        String image = recipeImage.getText().toString().trim();
        String difficulty = recipeDifficulty.getText().toString().trim();
        String category = recipeCategory.getText().toString().trim();
        String preparationTime = recipePreparationTime.getText().toString().trim();

        // Controlla se tutti i campi sono stati compilati
        if (name.isEmpty() || description.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || image.isEmpty() || difficulty.isEmpty() || category.isEmpty() || preparationTime.isEmpty()) {
            Toast.makeText(this, "Tutti i campi sono obbligatori", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crea un oggetto ricetta
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", name);
        recipe.put("description", description);
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("image", image);
        recipe.put("difficulty", difficulty);
        recipe.put("category", category);
        recipe.put("preparationTime", preparationTime);

        // Aggiungi la ricetta a Firebase Firestore
        db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Ricetta aggiunta con successo", Toast.LENGTH_SHORT).show();
                    finish(); // Chiude l'activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Errore nell'aggiunta della ricetta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
