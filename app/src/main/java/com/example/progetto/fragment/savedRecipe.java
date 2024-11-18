package com.example.progetto.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.recipe.RecipeViewModel;

import java.util.ArrayList;
import java.util.List;

public class savedRecipe extends Fragment {

    private RecyclerView recyclerViewRecipe;
    private RecipeAdapter recipeAdapter;
    private RecipeViewModel recipeViewModel;
    private View emptyStateView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saved_recipe_fragment, container, false);

        recyclerViewRecipe = view.findViewById(R.id.recyclerViewSavedRecipes);
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeAdapter = new RecipeAdapter();
        recyclerViewRecipe.setAdapter(recipeAdapter);

        emptyStateView = view.findViewById(R.id.emptyStateView);

        // Add a test recipe
        List<Recipe> sampleRecipes = new ArrayList<>();
        sampleRecipes.add(new Recipe("1", "Test Recipe", "Test Description", "https://via.placeholder.com/150"));
        recipeAdapter.setRecipes(sampleRecipes);
        recyclerViewRecipe.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        recipeViewModel.getRecipeListLiveData().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                Log.d("savedRecipe", "Number of recipes received: " + recipes.size());
                recipeAdapter.setRecipes(recipes);
                recyclerViewRecipe.setVisibility(View.VISIBLE);
                emptyStateView.setVisibility(View.GONE);
            } else {
                Log.d("savedRecipe", "No recipes found");
                recyclerViewRecipe.setVisibility(View.GONE);
                emptyStateView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}