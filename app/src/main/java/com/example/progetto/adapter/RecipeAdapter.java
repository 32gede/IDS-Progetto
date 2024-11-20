package com.example.progetto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private final Set<String> savedRecipeIds = new HashSet<>(); // Tracks saved recipes
    private final OnBookmarkClickListener bookmarkClickListener;

    public RecipeAdapter(List<Recipe> recipes, OnBookmarkClickListener bookmarkClickListener) {
        this.recipes = recipes;
        this.bookmarkClickListener = bookmarkClickListener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    public void setSavedRecipeIds(Set<String> savedRecipeIds) {
        this.savedRecipeIds.clear();
        this.savedRecipeIds.addAll(savedRecipeIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.titleTextView.setText(recipe.getName());
        holder.descriptionTextView.setText(recipe.getDescription());

        // Check if the recipe is already saved and set the bookmark icon
        if (savedRecipeIds.contains(recipe.getId())) {
            holder.bookmarkButton.setImageResource(R.drawable.baseline_bookmark_24);
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.baseline_bookmark_border_24);
        }

        // Set the listener for the bookmark button
        holder.bookmarkButton.setOnClickListener(v -> {
            boolean isSaved = savedRecipeIds.contains(recipe.getId());
            if (isSaved) {
                // Remove from saved and update icon
                savedRecipeIds.remove(recipe.getId());
                holder.bookmarkButton.setImageResource(R.drawable.baseline_bookmark_border_24);
            } else {
                // Add to saved and update icon
                savedRecipeIds.add(recipe.getId());
                holder.bookmarkButton.setImageResource(R.drawable.baseline_bookmark_24);
            }

            // Notify activity about the change
            if (bookmarkClickListener != null) {
                bookmarkClickListener.onBookmarkClick(recipe, !isSaved);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    // ViewHolder class updated to include the bookmark button
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageButton bookmarkButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton); // Add this in your layout
        }
    }

    // Interface to handle bookmark button clicks
    public interface OnBookmarkClickListener {
        void onBookmarkClick(Recipe recipe, boolean isSaved);
    }
}