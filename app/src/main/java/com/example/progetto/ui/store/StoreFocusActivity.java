package com.example.progetto.ui.store;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.adapter.IngredientsAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.SelectedIngredientUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.progetto.data.model.StoreUtils;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoreFocusActivity extends AppCompatActivity {
    private static final String TAG = "StoreFocusActivity";
    private ImageView imageView, backBtn;
    private TextView nameTextView, descriptionTextView, priceTextView;
    private RecyclerView ingredientsRecyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private Firestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_focus);

        Log.d(TAG, "onCreate: started");

        initializeViews();
        firestore = new Firestore();

        StoreUtils store = (StoreUtils) getIntent().getSerializableExtra("store");

        if (store != null) {
            Log.d(TAG, "onCreate: store data received");
            displayStoreDetails(store);
            ingredientsAdapter = new IngredientsAdapter(new ArrayList<>(), this);
            ingredientsRecyclerView.setAdapter(ingredientsAdapter);
            getIngredients(store.getId());
        } else {
            Log.d(TAG, "onCreate: no store data received");
            Toast.makeText(this, "No store data received", Toast.LENGTH_SHORT).show();
        }
    }

    private void getIngredients(String recipeId) {
        Log.d(TAG, "Fetching ingredients for recipe ID: " + recipeId);
        firestore.getSelectIngredients(recipeId, new FirestoreCallback<List<SelectedIngredientUtils>>() {
            @Override
            public void onSuccess(List<SelectedIngredientUtils> ingredients) {
                ingredientsAdapter.updateData(ingredients);
                Log.d(TAG, "Ingredients loaded: " + ingredients.size());
                if (Build.VERSION.SDK_INT >= 35) {
                    Log.d(TAG, "Ingredients: " + ingredients.get(0).getName() + ", " + ingredients.get(0).getQuantity());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch ingredients: " + e.getMessage());
                Toast.makeText(StoreFocusActivity.this, "Failed to fetch ingredients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: initializing views");
        imageView = findViewById(R.id.store_image);
        nameTextView = findViewById(R.id.store_name);
        descriptionTextView = findViewById(R.id.store_description);
        priceTextView = findViewById(R.id.store_price);
        ingredientsRecyclerView = findViewById(R.id.store_ingredients_recycler_view);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());
    }

    private void displayStoreDetails(StoreUtils store) {
        Log.d(TAG, "displayStoreDetails: displaying store details");
        Glide.with(this).load(store.getImage()).into(imageView);
        nameTextView.setText(store.getName());
        descriptionTextView.setText(store.getDescription());

        // Format the price
        if (store.getPrice() != null) {
            String formattedPrice = String.format("€%.2f", store.getPrice());
            priceTextView.setText(formattedPrice);
        } else {
            priceTextView.setText("€0.00"); // Default value for null price
        }
    }
}