package com.example.progetto.ui.recipe;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.progetto.data.model.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewModel extends ViewModel {
    private final MutableLiveData<List<Recipe>> recipeListLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RecipeViewModel() {
        fetchRecipes();
    }

    public LiveData<List<Recipe>> getRecipeListLiveData() {
        return recipeListLiveData;
    }

    private void fetchRecipes() {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recipe> recipes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipes.add(recipe);
                        }
                        recipeListLiveData.setValue(recipes);
                        Log.d("savedRecipe", "Ricette ricevute: " + recipes.size());
                        for (Recipe recipe : recipes) {
                            Log.d("savedRecipe", "Nome ricetta: " + recipe.getName());
                        }
                    } else {
                        Log.e("RecipeViewModel", "Error getting documents: ", task.getException());
                    }
                });
    }

    public LiveData<List<Recipe>> getAllRecipesLiveData() {
        MutableLiveData<List<Recipe>> liveData = new MutableLiveData<>();
        FirebaseFirestore.getInstance()
                .collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Recipe> recipes = queryDocumentSnapshots.toObjects(Recipe.class);
                        liveData.setValue(recipes);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> Log.e("RecipeViewModel", "Errore nel recupero delle ricette globali: " + e.getMessage()));
        return liveData;
    }

}

