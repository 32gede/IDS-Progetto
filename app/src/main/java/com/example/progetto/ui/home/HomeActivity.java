package com.example.progetto.ui.home;

import static com.example.progetto.data.model.NavigationUtils.updateNavSelection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.adapter.PopularRecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.store.storeActivity;
import com.example.progetto.data.model.SwipeGestureListener;

import com.example.progetto.ui.store.storeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private View homeBackgroundCircle, searchBackgroundCircle, fridgeBackgroundCircle, recipeBackgroundCircle;
    private ImageButton homeButton, profileButtonTop, storeButton, fridgeButton, recipeButton, notificationButton;
    private TextView titleText;

    private FirebaseFirestore firestore;
    private RecyclerView popularRecyclerView;
    private PopularRecipeAdapter adapter;
    private List<Recipe> recipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Inizializza Firestore
        firestore = FirebaseFirestore.getInstance();

        // Trova i riferimenti delle viste
        profileButtonTop = findViewById(R.id.profileButtonTop);
        homeBackgroundCircle = findViewById(R.id.homeBackgroundCircle);
        searchBackgroundCircle = findViewById(R.id.searchBackgroundCircle);
        fridgeBackgroundCircle = findViewById(R.id.fridgeBackgroundCircle);
        recipeBackgroundCircle = findViewById(R.id.recipeBackgroundCircle);
        homeButton = findViewById(R.id.homeButton);
        storeButton = findViewById(R.id.storeButton);
        fridgeButton = findViewById(R.id.fridgeButton);
        recipeButton = findViewById(R.id.recipeButton);
        titleText = findViewById(R.id.title);
        notificationButton = findViewById(R.id.notificationButton);
        titleText = findViewById(R.id.title);
        titleText.setText(getString(R.string.home));

        // Verifica che le viste siano state trovate
        if (homeBackgroundCircle != null && homeButton != null) {
            // Imposta il cerchio di sfondo solo per il pulsante Home
            updateNavSelection(R.id.homeButton, homeBackgroundCircle, null, null, null);
        } else {
            // Log per il debug (opzionale)
            System.out.println("homeBackgroundCircle o homeButton non trovati nel layout.");
        }

        // Configura RecyclerView
        popularRecyclerView = findViewById(R.id.popularRecyclerView);
        popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PopularRecipeAdapter(recipeList, this);
        popularRecyclerView.setAdapter(adapter);

        // Configura pulsanti
        configureNavigation();

        // Carica le ricette consigliate
        loadRecommendedRecipes();
    }

    private void configureNavigation() {
        // Listener per i pulsanti di navigazione
        profileButtonTop.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        storeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, storeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        fridgeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FridgeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        recipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RecipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Set up swipe gesture listener
        SwipeGestureListener swipeGestureListener = new SwipeGestureListener(this, storeActivity.class,HomeActivity.class);
        View mainView = findViewById(R.id.home);
        mainView.setOnTouchListener(swipeGestureListener);
    }

    private void loadRecommendedRecipes() {
        List<String> fridgeItems = getFridgeItemsFromFirestore();

        if (fridgeItems.isEmpty()) {
            // Se il frigo è vuoto, carica le ricette più recenti
            firestore.collection("recipes")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        recipeList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Recipe recipe = doc.toObject(Recipe.class);
                            recipeList.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> System.err.println("Errore nel caricamento delle ricette: " + e.getMessage()));
        } else {
            // Cerca ricette in base agli ingredienti nel frigo
            firestore.collection("recipes")
                    .whereArrayContainsAny("ingredients", fridgeItems)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        recipeList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Recipe recipe = doc.toObject(Recipe.class);
                            recipeList.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> System.err.println("Errore nel caricamento delle ricette consigliate: " + e.getMessage()));
        }
    }

    private List<String> getFridgeItemsFromFirestore() {
        List<String> fridgeItems = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("user_products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        fridgeItems.add(doc.getString("name")); // Supponiamo che "name" sia il nome del prodotto
                    }
                })
                .addOnFailureListener(e -> System.err.println("Errore nel caricamento degli ingredienti dal frigo: " + e.getMessage()));

        return fridgeItems;
    }
}
