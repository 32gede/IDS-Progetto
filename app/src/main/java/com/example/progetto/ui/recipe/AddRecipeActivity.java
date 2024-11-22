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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.adapter.IngredientsAdapter;
import com.example.progetto.data.model.ItemUtils;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText recipeName, recipeDescription, recipeSteps, recipeDifficulty, recipeCategory, recipePreparationTime;
    private RecyclerView recipeIngredients;
    private ImageView recipeImageView;
    private Button btnSubmitRecipe, btnSelectImage;

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ProgressBar progressBar;
    private IngredientsAdapter ingredientsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Inizializza Firebase
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("recipe_images");

        // Trova gli elementi UI
        initializeViews();
        setupIngredientsRecyclerView();

        // Listener per selezionare l'immagine
        btnSelectImage.setOnClickListener(v -> openImageChooser());

        // Listener per salvare la ricetta
        btnSubmitRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void setupIngredientsRecyclerView() {
        recipeIngredients = findViewById(R.id.ingredients_recycler_view);
        List<ItemUtils> ingredients = new ArrayList<>();

        // Recupera gli ingredienti da Firestore
        db.collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        ItemUtils item = queryDocumentSnapshots.getDocuments().get(i).toObject(ItemUtils.class);
                        if (item != null) {
                            ingredients.add(item);
                        }
                    }
                    ingredientsAdapter.notifyDataSetChanged();
                });

        // Inizializza il FlexboxLayoutManager
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW); // Direzione orizzontale delle righe
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START); // Allinea gli elementi all'inizio
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START); // Allinea gli elementi in alto

        // Imposta l'adattatore e il layout manager
        ingredientsAdapter = new IngredientsAdapter(ingredients);
        recipeIngredients.setLayoutManager(flexboxLayoutManager);
        recipeIngredients.setAdapter(ingredientsAdapter);
    }

    private void initializeViews() {
        recipeName = findViewById(R.id.recipe_name);
        recipeDescription = findViewById(R.id.recipe_description);
        recipeSteps = findViewById(R.id.recipe_steps);
        recipeImageView = findViewById(R.id.recipe_image_view);
        btnSelectImage = findViewById(R.id.btn_select_image);
        recipeDifficulty = findViewById(R.id.recipe_difficulty);
        recipeCategory = findViewById(R.id.recipe_category);
        recipePreparationTime = findViewById(R.id.recipe_preparation_time);
        btnSubmitRecipe = findViewById(R.id.btn_submit_recipe);
        progressBar = findViewById(R.id.progressBar);
    }

    private void saveRecipe() {
        String name = recipeName.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();
        List<ItemUtils> list_ingredients = ingredientsAdapter.getSelectedIngredients();
        String ingredients = "";
        for (ItemUtils ingredient : list_ingredients) {
            ingredients += ingredient.getName() + ", ";
        }
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
            uploadImageAndSaveRecipe(name, description, ingredients, steps, difficulty, category, preparationTime);
        } else {
            // Salva la ricetta senza immagine
            saveRecipeToFirestore(name, description, ingredients, steps, difficulty, category, preparationTime, null);
        }
    }

    private void uploadImageAndSaveRecipe(String name, String description, String ingredients, String steps,
                                          String difficulty, String category, String preparationTime) {
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
    }

    private void saveRecipeToFirestore(String name, String description, String ingredients, String steps,
                                       String difficulty, String category, String preparationTime, @Nullable String imageUrl) {
        // Genera un ID univoco
        String recipeId = db.collection("recipes").document().getId();

        // Crea una mappa per Firestore
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("id", recipeId); // Imposta l'ID come il nome del documento
        recipe.put("name", name);
        recipe.put("description", description);
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("image", imageUrl);
        recipe.put("difficulty", difficulty);
        recipe.put("category", category);
        recipe.put("preparationTime", preparationTime);

        // Salva nella collezione principale "recipes"
        db.collection("recipes").document(recipeId)
                .set(recipe)
                .addOnSuccessListener(aVoid -> saveRecipeForUser(recipeId, recipe)) // Chiama il metodo per salvare in recipes_user
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore nell'aggiunta della ricetta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRecipeForUser(String recipeId, Map<String, Object> recipe) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            // Aggiungi l'ID utente alla ricetta
            recipe.put("userId", userId);

            // Salva nella collezione "recipes_user"
            db.collection("recipes_user").document(recipeId)
                    .set(recipe)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Ricetta aggiunta con successo!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Errore nell'aggiunta della ricetta per l'utente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Errore: Utente non autenticato!", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null
                && data.getData() != null) {
            selectedImageUri = data.getData();
            // Usa Glide per mostrare l'immagine selezionata
            Glide.with(this)
                    .load(selectedImageUri)
                    .error(R.drawable.baseline_error_24)
                    .into(recipeImageView);
        }
    }
}
