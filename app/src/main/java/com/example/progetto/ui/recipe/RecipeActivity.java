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
import com.example.progetto.adapter.RecipePagerAdapter;
import com.example.progetto.data.model.NavigationUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.search.SearchActivity;

public class RecipeActivity extends AppCompatActivity {

    private ImageButton profileButtonRecipe, homeButtonRecipe, searchButtonRecipe, fridgeButtonRecipe, addButtonRecipe;
    private TextView titleRecipe;
    private ViewPager2 viewPagerRecipe;
    private TabLayout tabLayoutRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe);

        // Trova le view nel layout
        profileButtonRecipe = findViewById(R.id.profileButtonRecipe);
        homeButtonRecipe = findViewById(R.id.homeButton);
        searchButtonRecipe = findViewById(R.id.searchButton);
        fridgeButtonRecipe = findViewById(R.id.fridgeButton);
        addButtonRecipe = findViewById(R.id.addButtonRecipe);
        titleRecipe = findViewById(R.id.titleRecipe);
        titleRecipe.setText(getString(R.string.recipe));

        // Configura il ViewPager e il TabLayout
        tabLayoutRecipe = findViewById(R.id.tabLayoutRecipe);
        viewPagerRecipe = findViewById(R.id.viewPagerRecipe);

        // Imposta l'adapter per il ViewPager
        RecipePagerAdapter adapter = new RecipePagerAdapter(this);
        viewPagerRecipe.setAdapter(adapter);

        // Associa TabLayout e ViewPager
        new TabLayoutMediator(tabLayoutRecipe, viewPagerRecipe, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Salvate");
                        break;
                    case 1:
                        tab.setText("Globali");
                        break;
                }
            }
        }).attach();

        // Configura il comportamento dei pulsanti
        addButtonRecipe.setOnClickListener(v -> startActivity(new Intent(RecipeActivity.this, AddRecipeActivity.class)));
        profileButtonRecipe.setOnClickListener(v -> startActivity(new Intent(RecipeActivity.this, ProfileActivity.class)));
        homeButtonRecipe.setOnClickListener(v -> {
            startActivity(new Intent(RecipeActivity.this, HomeActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
        fridgeButtonRecipe.setOnClickListener(v -> {
            startActivity(new Intent(RecipeActivity.this, FridgeActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
        searchButtonRecipe.setOnClickListener(v -> {
            startActivity(new Intent(RecipeActivity.this, SearchActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Aggiorna la navigazione
        NavigationUtils.updateNavSelection(
                R.id.recipeButton,
                findViewById(R.id.homeBackgroundCircleRecipe),
                findViewById(R.id.searchBackgroundCircleRecipe),
                findViewById(R.id.fridgeBackgroundCircleRecipe),
                findViewById(R.id.recipeBackgroundCircleRecipe)
        );
    }
}
