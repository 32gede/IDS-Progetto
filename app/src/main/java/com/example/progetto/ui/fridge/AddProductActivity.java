package com.example.progetto.ui.fridge;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.adapter.ProductAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.data.model.UserProductUtils;
import com.example.progetto.data.model.Note.NotificationScheduler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddProductActivity extends AppCompatActivity implements ProductAdapter.OnProductSelectedListener {

    private FirebaseFirestore database;
    private Firestore firestore;
    private CollectionReference itemsCollection;
    private FirebaseAuth mAuth;  // Firebase Authentication instance
    private ProductAdapter productAdapter;
    private List<ItemUtils> productList = new ArrayList<>();
    private List<ItemUtils> filteredList = new ArrayList<>(); // For storing filtered products

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_fridge);

        // Initialize Firestore and Authentication
        firestore = new Firestore();
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        itemsCollection = database.collection("items");

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, filteredList, this);
        recyclerView.setAdapter(productAdapter);

        // Load items from Firestore
        loadItemsFromFirestore();

        // Setup back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, FridgeActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup search bar
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener to open CreateProductActivity
        FloatingActionButton addNewProductButton = findViewById(R.id.addNewProductButtonFridge);
        addNewProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, CreateProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadItemsFromFirestore() {
        itemsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ItemUtils product = document.toObject(ItemUtils.class);
                        product.setProductId(document.getId()); // Set ID to the document name
                        productList.add(product);
                    }

                    // Initialize filtered list with all items initially
                    filteredList.clear();
                    filteredList.addAll(productList);
                    productAdapter.updateProductList(filteredList);
                    Log.d("AddProductActivity", "Items loaded successfully from Firestore with document names as titles.");
                })
                .addOnFailureListener(e -> Log.e("AddProductActivity", "Failed to load items: " + e.getMessage()));
    }

    // Filter products based on search input
    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (ItemUtils product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        productAdapter.updateProductList(filteredList);
    }

    @Override
    public void onProductSelected(ItemUtils product) {
        // Initialize UserProductUtils with details from ItemUtils and user ID
        UserProductUtils userProduct = new UserProductUtils();
        userProduct.setUrl(product.getImageUrl());
        userProduct.setName(product.getName());

        // Get the current user's ID
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId != null) {
            userProduct.setUserId(userId);
            showProductDetailDialog(userProduct);
        } else {
            Log.e("AddProductActivity", "User ID is null. User might not be authenticated.");
        }
        Toast.makeText(this, "Product selected: " + product.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showProductDetailDialog(UserProductUtils product) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product_detail, null);
        EditText expiryDateEditText = dialogView.findViewById(R.id.expiry_date);
        EditText quantityEditText = dialogView.findViewById(R.id.quantity);

        // Set up an OnClickListener to show a DatePickerDialog when the expiry date EditText is clicked
        expiryDateEditText.setOnClickListener(v -> {
            // Get the current date as default values for the picker
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create and show the DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                // Set the selected date to the EditText
                String selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                expiryDateEditText.setText(selectedDate);
            }, year, month, day);

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Product Details")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String expiryDate = expiryDateEditText.getText().toString();
                    String quantityStr = quantityEditText.getText().toString();
                    int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);

                    product.setExpiryDate(expiryDate);
                    product.setQuantity(quantity);

                    saveProductDetailsToFirestore(product);
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void saveProductDetailsToFirestore(UserProductUtils product) {
        firestore.addSomething(product.toMap(), "user_products");
        Log.d("AddProductActivity", "Product details saved successfully with user-specific details.");

        // Show a success Toast notification
        Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();

        // Schedule the expiry notification
        Calendar expiryDate = Calendar.getInstance();
        // Assuming the expiry date is in the format "dd/MM/yyyy"
        String[] dateParts = product.getExpiryDate().split("/");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based in Calendar
        int year = Integer.parseInt(dateParts[2]);
        expiryDate.set(year, month, day);

        NotificationScheduler.scheduleExpiryNotification(this, product.getName(), product.getQuantity(), expiryDate);
    }
}