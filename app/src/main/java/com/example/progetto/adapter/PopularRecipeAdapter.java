package com.example.progetto.adapter;

import android.content.Context;
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

import java.util.List;

public class PopularRecipeAdapter extends RecyclerView.Adapter<PopularRecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Context context;

    public PopularRecipeAdapter(List<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.recipeName.setText(recipe.getName());
        holder.recipeDescription.setText(recipe.getDescription());

        // Carica l'immagine usando Glide (se è presente)
        Glide.with(context)
                .load(recipe.getImage())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.recipeImage);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName, recipeDescription;
        ImageView recipeImage;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.titleTextView);
            recipeDescription = itemView.findViewById(R.id.descriptionTextView);
            recipeImage = itemView.findViewById(R.id.recipeImageView);
        }
    }
}
