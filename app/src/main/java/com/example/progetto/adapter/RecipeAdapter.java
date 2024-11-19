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
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private OnBookmarkClickListener bookmarkClickListener;

    // Constructor updated to include the bookmark listener
    public RecipeAdapter(List<Recipe> recipes, OnBookmarkClickListener bookmarkClickListener) {
        this.recipes = recipes;
        this.bookmarkClickListener = bookmarkClickListener;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
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

        // Set the listener for the bookmark button
        holder.bookmarkButton.setOnClickListener(v -> {
            if (bookmarkClickListener != null) {
                bookmarkClickListener.onBookmarkClick(recipe);
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
        void onBookmarkClick(Recipe recipe);
    }
}
