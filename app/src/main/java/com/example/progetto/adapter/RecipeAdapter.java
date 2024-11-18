package com.example.progetto.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.recipe.ItemRecipeActivity;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes = new ArrayList<>(); // Lista di ricette

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla il layout della card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    // RecipeAdapter.java
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        Log.d("RecipeAdapter", "Popolamento card posizione: " + position);
        Log.d("RecipeAdapter", "Nome ricetta: " + recipe.getName());
        Log.d("RecipeAdapter", "Descrizione ricetta: " + recipe.getDescription());
        Log.d("RecipeAdapter", "Immagine URL: " + recipe.getImage());

        holder.titleTextView.setText(recipe.getName());
        holder.descriptionTextView.setText(recipe.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())
                .into(holder.recipeImageView);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ItemRecipeActivity.class);
            intent.putExtra("recipeId", recipe.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Restituisce la dimensione della lista
        return recipes.size();
    }

    public void setRecipes(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            Log.d("RecipeAdapter", "Lista ricevuta vuota o null. Imposto lista vuota.");
            this.recipes = new ArrayList<>(); // Imposta una lista vuota
        } else {
            Log.d("RecipeAdapter", "Imposto una lista con " + recipes.size() + " ricette.");
            this.recipes = recipes; // Assegna la lista delle ricette
        }
        notifyDataSetChanged(); // Notifica il RecyclerView del cambiamento dei dati
    }

    // ViewHolder per le card
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;
        ImageView recipeImageView;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            // Collega le view del layout della card
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
        }
    }
}
