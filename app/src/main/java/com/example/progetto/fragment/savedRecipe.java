package com.example.progetto.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.recipe.RecipeAdapter;
import com.example.progetto.ui.recipe.RecipeViewModel;

import java.util.List;

public class savedRecipe extends Fragment {

    private RecyclerView recyclerViewRecipe;
    private RecipeAdapter recipeAdapter;
    private RecipeViewModel recipeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saved_recipe_fragment, container, false);

        // Inizializza il RecyclerView
        recyclerViewRecipe = view.findViewById(R.id.recyclerViewSavedRecipes);
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeAdapter = new RecipeAdapter();
        recyclerViewRecipe.setAdapter(recipeAdapter);

        // Inizializza il ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        recipeViewModel.getRecipeListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                if (recipes != null && !recipes.isEmpty()) {
                    Log.d("savedRecipe", "Numero di ricette ricevute: " + recipes.size());
                    recipeAdapter.setRecipes(recipes);
                } else {
                    Log.d("savedRecipe", "Nessuna ricetta trovata");
                }
                recyclerViewRecipe.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
