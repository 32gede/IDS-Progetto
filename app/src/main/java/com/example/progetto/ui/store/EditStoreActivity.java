package com.example.progetto.ui.store;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.data.model.SelectedIngredientRecipeUtils;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.example.progetto.data.model.StoreUtils;
import com.example.progetto.adapter.SelectedIngredientsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EditStoreActivity extends AppCompatActivity {
    private static final String TAG = "EditStoreActivity";
    private EditText storeName, storeDescription, storePrezzo;
    private RecyclerView productsRecyclerView;
    private ImageView storeImageView;
    private Button btnSelectImage, btnSubmitStore;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private SelectedIngredientsAdapter ingredientsAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private StoreUtils store;
    List<SelectedIngredientUtils> selectIngredients;
    List<ItemUtils> ingredients;
    private Firestore firestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);
        firestore = new Firestore();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("store_images");
        Log.d(TAG, "Firebase references initialized");

        initializeViews();
        loadStoreData();
        setupListeners();

        selectIngredients = new ArrayList<>();
        ingredients = new ArrayList<>(); // Initialize the ingredients list

        if (store != null) {
            getSelectIngredients(store.getId());
        }
        Log.d(TAG, "Select Ingredients loaded awdaw: " + selectIngredients.size());

        getIngredients();

        ingredientsAdapter = new SelectedIngredientsAdapter(ingredients, selectIngredients);
        productsRecyclerView.setAdapter(ingredientsAdapter);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIngredients() {
        firestore.getIngredients(new FirestoreCallback<List<ItemUtils>>() {
            @Override
            public void onSuccess(List<ItemUtils> data) {
                ingredients.addAll(data);
                ingredientsAdapter.notifyDataSetChanged();
                Log.d(TAG, "Ingredients loaded: " + ingredients.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch ingredients: " + e.getMessage());
                Toast.makeText(EditStoreActivity.this, "Failed to fetch ingredients", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getSelectIngredients(String storeId) {
        firestore.getSelectIngredients(storeId, new FirestoreCallback<List<SelectedIngredientUtils>>() {
            @Override
            public void onSuccess(List<SelectedIngredientUtils> data) {
                selectIngredients.addAll(data);
                ingredientsAdapter.changeData(data);
                Log.d(TAG, "Select Ingredients loaded: " + selectIngredients.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch selected ingredients: " + e.getMessage());
                Toast.makeText(EditStoreActivity.this, "Failed to fetch selected ingredients", Toast.LENGTH_SHORT).show();
            }
        });
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

        Log.d(TAG, "Views initialized");
    }

    private void loadStoreData() {
        Intent intent = getIntent();
        store = (StoreUtils) intent.getSerializableExtra("store");
        if (store != null) {
            storeName.setText(store.getName());
            storeDescription.setText(store.getDescription());
            storePrezzo.setText(String.valueOf(store.getPrice()));
            Glide.with(this).load(store.getImage()).into(storeImageView);
        } else {
            Log.e(TAG, "Store data is null");
        }
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSubmitStore.setOnClickListener(v -> submitStore());
    }

    private void selectImage() {
        // Code to select image from gallery
    }

    private void submitStore() {
        String name = storeName.getText().toString();
        String description = storeDescription.getText().toString();
        double price = Double.parseDouble(storePrezzo.getText().toString());

        if (store != null) {
            store.setName(name);
            store.setDescription(description);
            store.setPrice(price);
            updateStoreInFirestore(store);
        } else {
            Log.e(TAG, "Store object is null in submitStore");
        }
    }

    private void updateStoreInFirestore(StoreUtils store) {
        DocumentReference storeRef = db.collection("stores").document(store.getId());
        storeRef.set(store)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Store updated successfully");
                    Toast.makeText(EditStoreActivity.this, "Store updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating store", e));
        firestore.removeSelectedIngredient(store.getId(), new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Selected ingredients removed successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to remove selected ingredients: " + e.getMessage());
            }
        });
        List<SelectedIngredientRecipeUtils> selectedProducts = new ArrayList<>();
        for (SelectedIngredientUtils ingredient : ingredientsAdapter.getSelectedIngredients()) {
            selectedProducts.add(new SelectedIngredientRecipeUtils(ingredient.getName(), ingredient.getQuantity(), store.getId(),2));
        }
        firestore.addSelectedIngredient(selectedProducts, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Selected ingredients added successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to add selected ingredients: " + e.getMessage());
            }
        });
    }
}