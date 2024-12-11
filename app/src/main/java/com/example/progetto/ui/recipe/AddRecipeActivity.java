package com.example.progetto.ui.recipe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.adapter.SelectedIngredientsAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.data.model.SelectedIngredientRecipeUtils;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
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
    private SelectedIngredientsAdapter ingredientsAdapter;
    private Firestore firestore;
    private String recipeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        firestore = new Firestore();
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
        // In your activity's onCreate method
        EditText preparationTimeEditText = findViewById(R.id.recipe_preparation_time);
        preparationTimeEditText.setOnClickListener(v -> {
            Dialog timePickerDialog = new Dialog(this);
            timePickerDialog.setContentView(R.layout.dialog_duration_picker);

            NumberPicker hourPicker = timePickerDialog.findViewById(R.id.hour_picker);
            NumberPicker minutePicker = timePickerDialog.findViewById(R.id.minute_picker);

            // Configura il NumberPicker per le ore (0-23)
            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(23);

            // Configura il NumberPicker per i minuti (0-59)
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(59);

            // Pulsante per confermare
            timePickerDialog.findViewById(R.id.btn_confirm).setOnClickListener(view -> {
                int hours = hourPicker.getValue();
                int minutes = minutePicker.getValue();

                // Imposta il tempo selezionato nell'EditText
                String selectedTime = String.format("%02d:%02d", hours, minutes);
                preparationTimeEditText.setText(selectedTime);
                timePickerDialog.dismiss();
            });

            // Mostra il Dialog
            timePickerDialog.show();
        });

    }

    private void setupIngredientsRecyclerView() {
        recipeIngredients = findViewById(R.id.ingredients_recycler_view);
        List<ItemUtils> ingredients = new ArrayList<>();


        firestore.getIngredients(new FirestoreCallback<List<ItemUtils>>() {
                                     @Override
                                     public void onSuccess(List<ItemUtils> result) {
                                         ingredients.addAll(result);
                                         ingredientsAdapter.updateData(result);

                                     }

                                     @Override
                                     public void onFailure(Exception e) {

                                     }
                                 }
        );

        // Inizializza il FlexboxLayoutManager
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW); // Direzione orizzontale delle righe
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START); // Allinea gli elementi all'inizio
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START); // Allinea gli elementi in alto

        // Imposta l'adattatore e il layout manager
        ingredientsAdapter = new SelectedIngredientsAdapter(ingredients);
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
        recipeDifficulty.setFilters(new InputFilter[]{new InputFilterMinMax(0, 9)});
        progressBar = findViewById(R.id.progressBar);
    }

    private void saveRecipe() {
        String name = recipeName.getText().toString().trim();
        String description = recipeDescription.getText().toString().trim();
        String steps = recipeSteps.getText().toString().trim();
        String difficulty = recipeDifficulty.getText().toString().trim();
        String category = recipeCategory.getText().toString().trim();
        String preparationTime = recipePreparationTime.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || steps.isEmpty() || difficulty.isEmpty() || category.isEmpty() || preparationTime.isEmpty()) {
            Toast.makeText(this, "Tutti i campi di testo sono obbligatori!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique recipe ID
        if (selectedImageUri != null) {
            // Upload the image to Firebase Storage
            uploadImageAndSaveRecipe(name, description, steps, difficulty, category, preparationTime);
        } else {
            // Save the recipe without an image
            saveRecipeToFirestore(name, description, steps, difficulty, category, preparationTime, null);
        }

        // Show the ProgressBar
        progressBar.setVisibility(View.VISIBLE);
    }

    private void uploadImageAndSaveRecipe(String name, String description, String steps,
                                          String difficulty, String category, String preparationTime) {

        firestore.uploadImage(selectedImageUri, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                saveRecipeToFirestore(name, description, steps, difficulty, category, preparationTime, imageUrl);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddRecipeActivity.this, "Errore nel caricamento dell'immagine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecipeToFirestore(String name, String description, String steps,
                                       String difficulty, String category, String preparationTime, @Nullable String imageUrl) {
        // Create a map for Firestore
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", name);
        recipe.put("description", description);
        recipe.put("steps", steps);
        recipe.put("image", imageUrl);
        recipe.put("difficulty", difficulty);
        recipe.put("ingredients", "Cocco");
        recipe.put("category", category);
        recipe.put("preparationTime", preparationTime);
        recipe.put("createdAt", FieldValue.serverTimestamp());
        recipeId = firestore.addSomething(recipe, "recipes");
        saveRecipeForUser(recipeId, recipe); // Call the method to save in recipes_user
        saveDefaultRating(recipeId);
        saveSelectIngredient(recipeId);
    }

    private void saveSelectIngredient(String recipeId) {
        List<SelectedIngredientRecipeUtils> list_ingredients = new ArrayList<>();
        for (SelectedIngredientUtils ingredient : ingredientsAdapter.getSelectedIngredients()) {
            list_ingredients.add(new SelectedIngredientRecipeUtils(ingredient.getName(), ingredient.getQuantity(), recipeId, 1));
        }
        firestore.addSelectedIngredient(list_ingredients,
                new FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Success
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddRecipeActivity.this, "Errore nell'aggiunta dell'ingrediente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDefaultRating(String recipeId) {
        Map<String, Object> rating = new HashMap<>();
        rating.put("recipeId", recipeId);
        rating.put("rating", 5);

        db.collection("ratings").document()
                .set(rating)
                .addOnSuccessListener(aVoid -> {
                    // Rating predefinito aggiunto con successo
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Errore nell'aggiunta del rating predefinito: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
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

