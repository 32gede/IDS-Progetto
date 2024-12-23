package com.example.progetto.ui.fridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.UserProductAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.UserProductUtils;
import com.example.progetto.data.model.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class FridgeActivity extends AppCompatActivity {

    private ImageButton addButton, notificationButton;
    private TextView titleText;
    private FirebaseAuth mAuth;
    private Firestore firestore = new Firestore();

    private RecyclerView recyclerViewFridge;
    private UserProductAdapter productAdapter;
    private List<UserProductUtils> fridgeProductList = new ArrayList<>();
    private List<UserProductUtils> filteredList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge);

        // Configura il BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.fridge_button);
            NavigationHelper.setupNavigation(this, bottomNavigationView);
        }

        // Inizializza Firestore e Auth
        mAuth = FirebaseAuth.getInstance();

        // Inizializza RecyclerView
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewFridge = findViewById(R.id.recyclerViewFridge);
        recyclerViewFridge.setLayoutManager(new GridLayoutManager(this, 2));

        // Configura l'adapter con i listener
        productAdapter = new UserProductAdapter(
                this,
                fridgeProductList,
                this::onProductSelected,
                this::onProductRemoved
        );
        recyclerViewFridge.setAdapter(productAdapter);

        // Inizializza le viste
        initializeViews();

        // Configura i listener di navigazione
        setNavigationListeners();

        // Configura SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadItemsFromFirestore);

        // Carica i prodotti da Firestore
        loadItemsFromFirestore();
    }

    private void initializeViews() {
        NavigationHelper.setupToolbar(findViewById(R.id.profileButton), findViewById(R.id.notificationButton), this);
        addButton = findViewById(R.id.addButton);
        titleText = findViewById(R.id.title);
        titleText.setText(getString(R.string.fridge));
    }

    private void setNavigationListeners() {
        addButton.setOnClickListener(v -> startActivity(new Intent(FridgeActivity.this, AddProductActivity.class)));
    }

    private void loadItemsFromFirestore() {
        swipeRefreshLayout.setRefreshing(true);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        Log.d("FridgeActivity", "User ID: " + userId);

        if (userId == null) {
            Log.e("FridgeActivity", "User ID is null. Cannot load items.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        firestore.getUserIngredients(userId, new FirestoreCallback<List<UserProductUtils>>() {
            @Override
            public void onSuccess(List<UserProductUtils> userProducts) {
                fridgeProductList.clear();

                for (UserProductUtils userProduct : userProducts) {
                    fridgeProductList.add(userProduct);
                }

                filteredList.clear();
                filteredList.addAll(fridgeProductList);
                productAdapter.updateProductList(filteredList);
                Log.d("FridgeActivity", "Items loaded successfully from Firestore. Total: " + fridgeProductList.size());

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("FridgeActivity", "Failed to load user products: " + e.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void onProductSelected(UserProductUtils userProduct) {
        Log.d("FridgeActivity", "Selected product: " + userProduct.getName());
        // Puoi gestire altre azioni per il prodotto selezionato
    }

    private void onProductRemoved(UserProductUtils userProduct) {

        firestore.removeUserProduct(userProduct.getId(), new FirestoreCallback<>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("FridgeActivity", "Product removed successfully: " + userProduct.getName());
                fridgeProductList.remove(userProduct);
                filteredList.remove(userProduct);
                productAdapter.updateProductList(filteredList);
                Log.d("FridgeActivity", "Product removed successfully from Firestore.");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("FridgeActivity", "Failed to remove product: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ricarica la ricetta e aggiorna la UI=
        loadItemsFromFirestore();
        Log.d("FridgeActivity", "Pagina aggiornata con successo!");
    }
}


