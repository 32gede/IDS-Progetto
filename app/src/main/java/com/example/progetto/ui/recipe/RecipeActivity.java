package com.example.progetto.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.progetto.data.model.NavigationUtils;

import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.search.SearchActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private View homeBackgroundCircleRecipe, searchBackgroundCircleRecipe, fridgeBackgroundCircleRecipe, recipeBackgroundCircleRecipe;
    private ImageButton homeButtonRecipe, profileButtonRecipe, searchButtonRecipe, fridgeButtonRecipe, recipeButtonProfile, addButtonRecipe;
    private TextView titleRecipe;
    private RecyclerView recyclerViewRecipe;
    private RecipeAdapter recipeAdapter;
    private ImageButton recipeButtonRecipe;
    private RecipeViewModel recipeViewModel;
    ViewPager2 viewPagerRecipe;
    TabLayout tabLayoutRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Find view references
        profileButtonRecipe = findViewById(R.id.profileButtonRecipe);
        recipeButtonRecipe = findViewById(R.id.recipeButton);
        homeBackgroundCircleRecipe = findViewById(R.id.homeBackgroundCircleRecipe);
        searchBackgroundCircleRecipe = findViewById(R.id.searchBackgroundCircleRecipe);
        fridgeBackgroundCircleRecipe = findViewById(R.id.fridgeBackgroundCircleRecipe);
        recipeBackgroundCircleRecipe = findViewById(R.id.recipeBackgroundCircleRecipe);
        homeButtonRecipe = findViewById(R.id.homeButton);
        searchButtonRecipe = findViewById(R.id.searchButton);
        fridgeButtonRecipe = findViewById(R.id.fridgeButton);
        titleRecipe = findViewById(R.id.titleRecipe);
        titleRecipe.setText(getString(R.string.recipe));
        addButtonRecipe = findViewById(R.id.addButtonRecipe);

        tabLayoutRecipe = findViewById(R.id.tabLayoutRecipe);
        viewPagerRecipe = findViewById(R.id.viewPagerRecipe);
        NavigationUtils.updateNavSelection(R.id.recipeButton, homeBackgroundCircleRecipe, searchBackgroundCircleRecipe, fridgeBackgroundCircleRecipe, recipeBackgroundCircleRecipe);

        new TabLayoutMediator(tabLayoutRecipe, viewPagerRecipe, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Salte"); // Titolo per il primo tab
                        break;
                    case 1:
                        tab.setText("Globali"); // Titolo per il secondo tab
                        break;
                }
            }
        }).attach();

        // Set up RecyclerView
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(this));
        recipeAdapter = new RecipeAdapter();
        recyclerViewRecipe.setAdapter(recipeAdapter);

        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        recipeViewModel.getRecipeListLiveData().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                recipeAdapter.setRecipes(recipes);
                recyclerViewRecipe.setVisibility(View.VISIBLE);
            }
        });

        // Add Recipe Button Listener
        addButtonRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipeActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });

        // Profile Button Listener
        profileButtonRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Home Button Listener
        homeButtonRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipeActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Fridge Button Listener
        fridgeButtonRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipeActivity.this, FridgeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // Search Button Listener
        searchButtonRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecipeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
    }
}
