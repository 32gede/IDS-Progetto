package com.example.progetto.ui.store;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.progetto.data.model.StoreUtils;

public class StoreFocusActivity extends AppCompatActivity {
    private static final String TAG = "StoreFocusActivity";
    private ImageView imageView;
    private TextView nameTextView, descriptionTextView, priceTextView;
    private FirebaseFirestore databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_focus);

        Log.d(TAG, "onCreate: started");

        initializeViews();

        databaseReference = FirebaseFirestore.getInstance();

        StoreUtils store = (StoreUtils) getIntent().getSerializableExtra("store");

        if (store != null) {
            Log.d(TAG, "onCreate: store data received");
            displayStoreDetails(store);
        } else {
            Log.d(TAG, "onCreate: no store data received");
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: initializing views");
        imageView = findViewById(R.id.store_image);
        nameTextView = findViewById(R.id.store_name);
        descriptionTextView = findViewById(R.id.store_description);
        priceTextView = findViewById(R.id.store_price);
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