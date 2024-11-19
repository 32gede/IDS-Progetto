package com.example.progetto.ui.recipe;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.RecipeUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText recipeName, recipeDescription, recipeIngredients, recipeSteps, recipeDifficulty, recipeCategory, recipePreparationTime;
    private ImageView recipeImageView;
    private Button btnSubmitRecipe, btnSelectImage;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Inizializza Firebase
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("recipe_images");

        // Trova gli elementi UI
        recipeName = findViewById(R.id.recipe_name);
        recipeDescription = findViewById(R.id.recipe_description);
        recipeIngredients = findViewById(R.id.recipe_ingredients);
        recipeSteps = findViewById(R.id.recipe_steps);
        recipeImageView = findViewById(R.id.recipe_image_view);
        btnSelectImage = findViewById(R.id.btn_select_image);
        recipeDifficulty = findViewById(R.id.recipe_difficulty);
        recipeCategory = findViewById(R.id.recipe_category);
        recipePreparationTime = findViewById(R.id.recipe_preparation_time);
        btnSubmitRecipe = findViewById(R.id.btn_submit_recipe);
        progressBar = findViewById(R.id.progressBar);

        // Listener per selezionare l'immagine
        btnSelectImage.setOnClickListener(v -> openImageChooser());

        // Listener per salvare la ricetta
        btnSubmitRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void saveRecipe() {
        String name = recipeName.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();
        String ingredients = recipeIngredients.getText().toString().trim();
        String steps = recipeSteps.getText().toString().trim();
        String difficulty = recipeDifficulty.getText().toString().trim();
        String category = recipeCategory.getText().toString().trim();
        String preparationTime = recipePreparationTime.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || difficulty.isEmpty() || category.isEmpty() || preparationTime.isEmpty()) {
            Toast.makeText(this, "Tutti i campi di testo sono obbligatori!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostra la ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            // Carica l'immagine su Firebase Storage
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveRecipeToFirestore(name, description, ingredients, steps, difficulty, category, preparationTime, imageUrl);
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Errore nel caricamento dell'immagine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Salva la ricetta senza immagine
            saveRecipeToFirestore(name, description, ingredients, steps, difficulty, category, preparationTime, null);
        }
    }

    private void saveRecipeToFirestore(String name, String description, String ingredients, String steps,
                                       String difficulty, String category, String preparationTime, @Nullable String imageUrl) {
        // Crea un oggetto ricetta
        Recipe appo = new Recipe(name, description, ingredients, steps, difficulty, category, preparationTime, imageUrl);
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", name);
        recipe.put("description", description);
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("image", imageUrl); // Può essere null
        recipe.put("difficulty", difficulty);
        recipe.put("category", category);
        recipe.put("preparationTime", preparationTime);

        // Salva la ricetta in Firestore
        db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ricetta aggiunta con successo!", Toast.LENGTH_SHORT).show();
                    finish(); // Chiude l'activity
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore nell'aggiunta della ricetta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        recipe.put("userId", userId);
        db.collection("recipes_user")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ricetta Utente aggiunta con successo!", Toast.LENGTH_SHORT).show();
                    finish(); // Chiude l'activity
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore nell'aggiunta della ricetta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            recipeImageView.setImageURI(selectedImageUri); // Mostra l'immagine selezionata
        }
    }
}
