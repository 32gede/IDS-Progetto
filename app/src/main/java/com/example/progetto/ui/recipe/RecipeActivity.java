package com.example.progetto.ui.recipe;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.data.model.UserProductUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private TabLayout tabLayout;
    private RecipeAdapter adapter;

    // Liste di esempio per le schede
    private List<Recipe> savedRecipes;
    private List<Recipe> globalRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Trova le viste
        recyclerView = findViewById(R.id.recyclerViewRecipes);
        tabLayout = findViewById(R.id.tabLayoutRecipe);

        // Configura il RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>()); // Inizialmente vuoto
        recyclerView.setAdapter(adapter);

        // Crea i dati di esempio
        setupSampleData();

        // Configura il TabLayout
        setupTabLayout();
    }

    private void setupSampleData() {

        globalRecipes = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    globalRecipes.clear(); // Clear current product list

                    // Populate the list with UserProductUtils objects
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe userProduct = document.toObject(Recipe.class);
                        globalRecipes.add(userProduct);
                    }
                    Log.d("RecipeActivity", "Items loaded successfully from Firestore. Total: " + globalRecipes.size());

                    // Stop the refreshing animation
                })
                .addOnFailureListener(e -> {
                    Log.e("RecipeActivity", "Failed to load user products: " + e.getMessage());
                });

        // Dati per la scheda "Saved Recipes"
        savedRecipes = new ArrayList<>();
        savedRecipes.add(new Recipe("Pasta Carbonara", "Una classica ricetta italiana", "Descrizione", ""));
        savedRecipes.add(new Recipe("Tiramisu", "Un delizioso dessert al caffè", "Descrizione", ""));

    }

    private void setupTabLayout() {
        // Aggiungi le schede
        tabLayout.addTab(tabLayout.newTab().setText("Saved Recipes"));
        tabLayout.addTab(tabLayout.newTab().setText("Global Recipes"));

        // Gestione del click sulle schede
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // Mostra le ricette salvate
                    adapter.setRecipes(savedRecipes);
                } else if (position == 1) {
                    // Mostra le ricette globali
                    adapter.setRecipes(globalRecipes);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Mostra i dati della prima scheda di default
        tabLayout.selectTab(tabLayout.getTabAt(0));
        adapter.setRecipes(savedRecipes);
    }
}
