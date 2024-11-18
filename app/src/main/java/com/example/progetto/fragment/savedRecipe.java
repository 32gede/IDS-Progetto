package com.example.progetto.fragment;

import static java.security.AccessController.getContext;

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
import com.example.progetto.ui.recipe.RecipeViewModel;

public class savedRecipe extends Fragment {

    private RecyclerView recyclerViewRecipe;
    private RecipeAdapter recipeAdapter;
    private RecipeViewModel recipeViewModel;
    private View emptyStateView; // Per gestire lo stato vuoto

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.saved_recipe_fragment, container, false);

        // Inizializza il RecyclerView
        recyclerViewRecipe = view.findViewById(R.id.recyclerViewSavedRecipes);
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeAdapter = new RecipeAdapter();
        recyclerViewRecipe.setAdapter(recipeAdapter);

        // Inizializza la vista di stato vuoto
        emptyStateView = view.findViewById(R.id.emptyStateView);

        // Inizializza il ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Osserva i dati del ViewModel
        recipeViewModel.getRecipeListLiveData().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                Log.d("savedRecipe", "Numero di ricette ricevute: " + recipes.size());
                recipeAdapter.setRecipes(recipes);
                recyclerViewRecipe.setVisibility(View.VISIBLE);
                emptyStateView.setVisibility(View.GONE);
            } else {
                Log.d("savedRecipe", "Nessuna ricetta trovata");
                recyclerViewRecipe.setVisibility(View.GONE);
                emptyStateView.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
