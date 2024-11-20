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
    private Set<String> savedRecipeIds;
    private final OnBookmarkClickListener bookmarkClickListener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Recipe recipe, boolean isSaved);
    }

    public RecipeAdapter(List<Recipe> recipes, OnBookmarkClickListener bookmarkClickListener) {
        this.recipes = recipes;
        this.bookmarkClickListener = bookmarkClickListener;
        this.savedRecipeIds = new HashSet<>();
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    public void setSavedRecipeIds(Set<String> savedRecipeIds) {
        this.savedRecipeIds = savedRecipeIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe, savedRecipeIds.contains(recipe.getId()));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView recipeTitle;
        private final ImageButton bookmarkButton;

        RecipeViewHolder(View itemView) {
            super(itemView);
            recipeTitle = itemView.findViewById(R.id.titleTextView);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
        }

        void bind(Recipe recipe, boolean isSaved) {
            recipeTitle.setText(recipe.getName());
            bookmarkButton.setImageResource(isSaved ? R.drawable.baseline_bookmark_24 : R.drawable.baseline_bookmark_border_24);

            bookmarkButton.setOnClickListener(v -> {
                boolean newSavedState = !isSaved;
                bookmarkClickListener.onBookmarkClick(recipe, newSavedState);
                bookmarkButton.setImageResource(newSavedState ? R.drawable.baseline_bookmark_24 : R.drawable.baseline_bookmark_border_24);
            });
        }
    }
}