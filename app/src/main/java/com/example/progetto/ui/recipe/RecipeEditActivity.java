package com.example.progetto.ui.recipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.adapter.IngredientsAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class RecipeEditActivity extends AppCompatActivity {

    private static final String TAG = "RecipeEditActivity";

    private EditText recipeName, recipeDescription, recipeSteps, recipeDifficulty, recipeCategory, recipePreparationTime;
    private ImageView recipeImageView;
    private Button btnSelectImage, btnSubmitRecipe;
    private RecyclerView ingredientsRecyclerView;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private IngredientsAdapter ingredientsAdapter;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private Recipe recipe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        initializeViews();

        // Inizializza Firebase
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Recupera la ricetta passata dall'activity precedente
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe != null) {
            populateFields(recipe);
        }

        // Listener per selezionare l'immagine
        btnSelectImage.setOnClickListener(v -> selectImage());

        // Listener per salvare la ricetta
        btnSubmitRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void initializeViews() {
        recipeName = findViewById(R.id.recipe_name);
        recipeDescription = findViewById(R.id.recipe_description);
        recipeSteps = findViewById(R.id.recipe_steps);
        recipeDifficulty = findViewById(R.id.recipe_difficulty);
        recipeCategory = findViewById(R.id.recipe_category);
        recipePreparationTime = findViewById(R.id.recipe_preparation_time);
        recipeImageView = findViewById(R.id.recipe_image_view);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSubmitRecipe = findViewById(R.id.btn_submit_recipe);
        progressBar = findViewById(R.id.progressBar);

        ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view);
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>(), this);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);
    }

    private void populateFields(Recipe recipe) {
        recipeName.setText(recipe.getName());
        recipeDescription.setText(recipe.getDescription());
        recipeSteps.setText(recipe.getSteps());
        recipeDifficulty.setText(recipe.getDifficulty());
        recipeCategory.setText(recipe.getCategory());
        recipePreparationTime.setText(recipe.getPreparationTime());

        // Carica immagine se disponibile
        if (recipe.getImage() != null) {
            Glide.with(this)
                    .load(recipe.getImage())
                    .into(recipeImageView);
        }

        // Carica gli ingredienti
        fetchIngredients(recipe.getId());
    }

    private void fetchIngredients(String recipeId) {
    firestore.collection("SelectedIngredient")
            .whereEqualTo("recipeId", recipeId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<SelectedIngredientUtils> ingredients = new ArrayList<>();
                queryDocumentSnapshots.forEach(document -> {
                    String ingredientName = document.getString("name");
                    if (ingredientName != null) {
                        SelectedIngredientUtils ingredient = new SelectedIngredientUtils();
                        ingredient.setName(ingredientName);
                        ingredients.add(ingredient);
                    }
                });
                ingredientsAdapter.updateData(ingredients);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch ingredients: " + e.getMessage()));
}

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    recipeImageView.setImageURI(selectedImageUri);
                }
            });

    private void saveRecipe() {
        String name = recipeName.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();
        String steps = recipeSteps.getText().toString().trim();
        String difficulty = recipeDifficulty.getText().toString().trim();
        String category = recipeCategory.getText().toString().trim();
        String preparationTime = recipePreparationTime.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Riempi tutti i campi richiesti", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Salva immagine se selezionata
        if (selectedImageUri != null) {
            StorageReference imageRef = storageReference.child("recipe_images/" + recipe.getId() + ".jpg");
            imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        recipe.setImage(uri.toString());
                        updateRecipeInFirestore(name, description, steps, difficulty, category, preparationTime);
                    })
            ).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Impossibile caricare l'immagine", Toast.LENGTH_SHORT).show();
            });
        } else {
            updateRecipeInFirestore(name, description, steps, difficulty, category, preparationTime);
        }
    }

    private void updateRecipeInFirestore(String name, String description, String steps, String difficulty, String category, String preparationTime) {
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setSteps(steps);
        recipe.setDifficulty(difficulty);
        recipe.setCategory(category);
        recipe.setPreparationTime(preparationTime);

        firestore.collection("recipes").document(recipe.getId())
                .set(recipe)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ricetta aggiornata con successo!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore durante l'aggiornamento", Toast.LENGTH_SHORT).show();
                });
    }
}
