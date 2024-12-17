package com.example.progetto.ui.store;

import android.app.Activity;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStoreActivity extends AppCompatActivity {

    private static final String TAG = "AddStoreActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText storeName, storeDescription, storePrezzo;
    private RecyclerView productsRecyclerView;
    private ImageView storeImageView;
    private Button btnSelectImage, btnSubmitStore;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private SelectedIngredientsAdapter ingredientsAdapter;

    private FirebaseFirestore db;
    private Firestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);
        firestore = new Firestore();

        initializeViews();
        setupProductsRecyclerView();

        btnSelectImage.setOnClickListener(v -> openImageChooser());
        btnSubmitStore.setOnClickListener(v -> saveStore());
    }

    private void initializeViews() {
        storeName = findViewById(R.id.store_name);
        storeDescription = findViewById(R.id.store_description);
        storePrezzo = findViewById(R.id.store_prezzo);
        productsRecyclerView = findViewById(R.id.products_recycler_view);
        storeImageView = findViewById(R.id.store_image_view);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSubmitStore = findViewById(R.id.btn_submit_store);
        progressBar = findViewById(R.id.progressBar);
        ImageView backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("store_images");

        Log.d(TAG, "Views initialized");
    }

    private void setupProductsRecyclerView() {
        productsRecyclerView = findViewById(R.id.products_recycler_view);
        List<ItemUtils> ingredients = new ArrayList<>();

        // Recupera gli ingredienti da Firestore
        firestore.getIngredients(new FirestoreCallback<>() {
            @Override
            public void onSuccess(List<ItemUtils> result) {
                ingredientsAdapter.updateData(result);
                Log.d(TAG, "Ingredients loaded successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading ingredients: " + e.getMessage());
            }
        });

        // Inizializza il FlexboxLayoutManager
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW); // Direzione orizzontale delle righe
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START); // Allinea gli elementi all'inizio
        flexboxLayoutManager.setAlignItems(AlignItems.FLEX_START); // Allinea gli elementi in alto

        // Imposta l'adattatore e il layout manager
        ingredientsAdapter = new SelectedIngredientsAdapter(ingredients);
        productsRecyclerView.setLayoutManager(flexboxLayoutManager);
        productsRecyclerView.setAdapter(ingredientsAdapter);
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .error(R.drawable.baseline_error_24)
                    .into(storeImageView);
            Log.d(TAG, "Image selected: " + selectedImageUri.toString());
        } else {
            Log.e(TAG, "Image selection failed or canceled");
        }
    }

    private void saveStore() {
        String name = storeName.getText().toString().trim();
        String description = storeDescription.getText().toString().trim();
        String prezzoStr = storePrezzo.getText().toString().trim();


        if (name.isEmpty() || description.isEmpty() || prezzoStr.isEmpty()) {
            Toast.makeText(this, "Tutti i campi di testo sono obbligatori!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Validation failed: Some fields are empty");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Inserire un numero valido per il prezzo!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Price conversion failed: Invalid number format");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null) {
            uploadImageAndSaveStore(name, description, prezzo);
        } else {
            saveStoreToFirestore(name, description, prezzo, null);
        }
    }


    private void uploadImageAndSaveStore(String name, String description, double prezzo) {
        firestore.uploadImage(selectedImageUri, new FirestoreCallback<>() {
            @Override
            public void onSuccess(String imageUrl) {
                saveStoreToFirestore(name, description, prezzo, imageUrl);
                Log.d(TAG, "Image uploaded successfully: " + imageUrl);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddStoreActivity.this, "Errore nel caricamento dell'immagine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Image upload failed: " + e.getMessage());
            }
        });
    }

    private void saveStoreToFirestore(String name, String description, double prezzo, @Nullable String imageUrl) {
        String storeId = db.collection("stores").document().getId();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        Map<String, Object> store = new HashMap<>();
        store.put("id", storeId);
        store.put("name", name);
        store.put("description", description);
        store.put("price", prezzo); // Adesso è un numero
        store.put("image", imageUrl);
        store.put("userId", userId);

        db.collection("stores").document(storeId)
                .set(store)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Store aggiunto con successo!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Store added successfully");
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Errore nell'aggiunta dello store: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding store: " + e.getMessage());
                });
        List<SelectedIngredientRecipeUtils> selectedProducts = new ArrayList<>();
        for (SelectedIngredientUtils ingredient : ingredientsAdapter.getSelectedIngredients()) {
            selectedProducts.add(new SelectedIngredientRecipeUtils(ingredient.getName(), ingredient.getQuantity(), storeId,2));
        }
        for (SelectedIngredientRecipeUtils ingredient : selectedProducts) {
            db.collection("SelectedIngredient").add(ingredient)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Errore nell'aggiunta dell'ingrediente: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}