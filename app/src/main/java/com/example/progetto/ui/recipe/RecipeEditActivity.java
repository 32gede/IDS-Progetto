package com.example.progetto.ui.recipe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
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
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.SelectedIngredientRecipeUtils;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class RecipeEditActivity extends AppCompatActivity {
    private static final String TAG = "RecipeEditActivity";

    // UI Elements
    private EditText recipeName, recipeDescription, recipeSteps, recipeDifficulty, recipeCategory, recipePreparationTime;
    private RecyclerView recipeIngredients;
    private ImageView recipeImageView;
    private Button btnSubmitRecipe, btnSelectImage;
    private ProgressBar progressBar;

    // Firebase and Adapter
    private Firestore firestore;
    private Uri selectedImageUri;
    private SelectedIngredientsAdapter ingredientsAdapter;

    // Recipe Data
    private Recipe recipe;
    private List<SelectedIngredientUtils> selectedIngredientsList = new ArrayList<>();

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        firestore = new Firestore();
        initializeViews();
        setupIngredientsRecyclerView();
        loadRecipeDataFromIntent();
        setupClickListeners();
    }

    // --- INITIALIZATION ---

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

        recipeDifficulty.setFilters(new InputFilter[]{new InputFilterMinMax(0, 9)});
    }

    private void setupIngredientsRecyclerView() {
        recipeIngredients = findViewById(R.id.ingredients_recycler_view);
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START);

        ingredientsAdapter = new SelectedIngredientsAdapter(new ArrayList<>());
        recipeIngredients.setLayoutManager(flexboxLayoutManager);
        recipeIngredients.setAdapter(ingredientsAdapter);

        loadIngredients();
    }

    private void loadIngredients() {
        firestore.getIngredients(new FirestoreCallback<List<ItemUtils>>() {
            @Override
            public void onSuccess(List<ItemUtils> ingredients) {
                ingredientsAdapter.updateData(ingredients);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RecipeEditActivity.this, "Errore nel caricamento degli ingredienti.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeDataFromIntent() {
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (recipe != null) {
            loadRecipeData();
            getSelectedIngredients(recipe.getId());
        } else {
            Log.e(TAG, "Ricetta non trovata nell'intento!");
        }
    }

    private void setupClickListeners() {
        btnSelectImage.setOnClickListener(v -> openImageChooser());
        btnSubmitRecipe.setOnClickListener(v -> validateAndUpdateRecipe());
        setupTimePicker();
    }

    // --- DATA LOADING ---

    private void loadRecipeData() {
        recipeName.setText(recipe.getName());
        recipeDescription.setText(recipe.getDescription());
        recipeSteps.setText(recipe.getSteps());
        recipeDifficulty.setText(recipe.getDifficulty());
        recipeCategory.setText(recipe.getCategory());
        recipePreparationTime.setText(recipe.getPreparationTime());
        Glide.with(this).load(recipe.getImage()).into(recipeImageView);
    }

    private void getSelectedIngredients(String recipeId) {
        firestore.getSelectIngredients(recipeId, new FirestoreCallback<List<SelectedIngredientUtils>>() {
            @Override
            public void onSuccess(List<SelectedIngredientUtils> data) {
                selectedIngredientsList.addAll(data);
                ingredientsAdapter.changeData(data);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(RecipeEditActivity.this, "Errore nel caricamento degli ingredienti selezionati.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- VALIDATION & UPDATE ---

    private void validateAndUpdateRecipe() {
        if (!validateFields()) {
            Toast.makeText(this, "Tutti i campi sono obbligatori!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Recipe updatedRecipe = createUpdatedRecipe();
        List<SelectedIngredientRecipeUtils> updatedIngredients = createUpdatedIngredientsList();

        if (selectedImageUri != null) {
            uploadImageAndSaveRecipe(updatedRecipe, updatedIngredients);
        } else {
            saveRecipeToFirestore(updatedRecipe, updatedIngredients);
        }
    }

    private boolean validateFields() {
        return !recipeName.getText().toString().trim().isEmpty()
                && !recipeDescription.getText().toString().trim().isEmpty()
                && !recipeSteps.getText().toString().trim().isEmpty()
                && !recipeDifficulty.getText().toString().trim().isEmpty()
                && !recipeCategory.getText().toString().trim().isEmpty()
                && !recipePreparationTime.getText().toString().trim().isEmpty();
    }

    private Recipe createUpdatedRecipe() {
        return new Recipe(
                recipe.getId(),
                recipeName.getText().toString().trim(),
                recipeDescription.getText().toString().trim(),
                selectedImageUri != null ? selectedImageUri.toString() : recipe.getImage(),
                recipeDifficulty.getText().toString().trim(),
                recipeSteps.getText().toString().trim(),
                recipeCategory.getText().toString().trim(),
                recipePreparationTime.getText().toString().trim(),
                recipe.getAverageRating()
        );
    }

    private List<SelectedIngredientRecipeUtils> createUpdatedIngredientsList() {
        List<SelectedIngredientRecipeUtils> updatedIngredients = new ArrayList<>();
        for (SelectedIngredientUtils ingredient : ingredientsAdapter.getSelectedIngredients()) {
            updatedIngredients.add(new SelectedIngredientRecipeUtils(ingredient.getName(), ingredient.getQuantity(), recipe.getId(), 1));
        }
        return updatedIngredients;
    }

    private void uploadImageAndSaveRecipe(Recipe updatedRecipe, List<SelectedIngredientRecipeUtils> updatedIngredients) {
        firestore.uploadImage(selectedImageUri, new FirestoreCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                updatedRecipe.setImage(imageUrl);
                saveRecipeToFirestore(updatedRecipe, updatedIngredients);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RecipeEditActivity.this, "Errore nel caricamento dell'immagine.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveRecipeToFirestore(Recipe updatedRecipe, List<SelectedIngredientRecipeUtils> updatedIngredients) {
        firestore.updateRecipe(updatedRecipe.getId(), updatedRecipe, updatedIngredients, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RecipeEditActivity.this, "Ricetta aggiornata con successo!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RecipeEditActivity.this, "Errore nell'aggiornamento della ricetta.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- IMAGE PICKER ---

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleziona immagine"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).error(R.drawable.baseline_error_24).into(recipeImageView);
        }
    }

    // --- TIME PICKER ---

    private void setupTimePicker() {
        recipePreparationTime.setOnClickListener(v -> {
            Dialog timePickerDialog = new Dialog(this);
            timePickerDialog.setContentView(R.layout.dialog_duration_picker);

            NumberPicker hourPicker = timePickerDialog.findViewById(R.id.hour_picker);
            NumberPicker minutePicker = timePickerDialog.findViewById(R.id.minute_picker);

            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(23);
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(59);

            timePickerDialog.findViewById(R.id.btn_confirm).setOnClickListener(view -> {
                String selectedTime = String.format("%02d:%02d", hourPicker.getValue(), minutePicker.getValue());
                recipePreparationTime.setText(selectedTime);
                timePickerDialog.dismiss();
            });

            timePickerDialog.show();
        });
    }
}
