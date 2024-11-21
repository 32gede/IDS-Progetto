package com.example.progetto.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.recipe.RecipeFocusActivity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private Set<String> savedRecipeIds;
    private final OnBookmarkClickListener bookmarkClickListener;
    private final Context context;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Recipe recipe, boolean isSaved);
    }

    public RecipeAdapter(List<Recipe> recipes, OnBookmarkClickListener bookmarkClickListener, Context context) {
        this.recipes = recipes;
        this.bookmarkClickListener = bookmarkClickListener;
        this.savedRecipeIds = new HashSet<>();
        this.context = context;
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = recipes.get(position);
                    Intent intent = new Intent(context, RecipeFocusActivity.class);
                    intent.putExtra("recipe", recipe);
                    context.startActivity(intent);
                }
            });
        }

        void bind(Recipe recipe, boolean isSaved) {
            recipeTitle.setText(recipe.getName());

            // Controlla se la ricetta è salvata nel Set globale e imposta lo stato iniziale
            boolean isCurrentlySaved = savedRecipeIds.contains(recipe.getId());
            bookmarkButton.setImageResource(isCurrentlySaved ?
                    R.drawable.baseline_bookmark_24 :
                    R.drawable.baseline_bookmark_border_24);

            // Aggiungi il listener per gestire il clic
            bookmarkButton.setOnClickListener(v -> {
                boolean newSavedState;

                // Alterna lo stato di salvataggio
                if (savedRecipeIds.contains(recipe.getId())) {
                    savedRecipeIds.remove(recipe.getId()); // Rimuovi dai salvati
                    newSavedState = false;
                } else {
                    savedRecipeIds.add(recipe.getId()); // Aggiungi ai salvati
                    newSavedState = true;
                }

                // Aggiorna il database tramite il listener fornito
                bookmarkClickListener.onBookmarkClick(recipe, newSavedState);

                // Cambia immediatamente l'icona
                bookmarkButton.setImageResource(newSavedState ?
                        R.drawable.baseline_bookmark_24 :
                        R.drawable.baseline_bookmark_border_24);
            });
        }


    }
}