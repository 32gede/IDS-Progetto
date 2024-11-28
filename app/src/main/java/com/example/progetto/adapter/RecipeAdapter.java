package com.example.progetto.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.recipe.RecipeFocusActivity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

    public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private Set<String> savedRecipeIds;
    private final OnBookmarkClickListener bookmarkClickListener;
    private final Context context;
    private boolean isHomeActivity;
    private ImageButton bookmarkButton;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Recipe recipe, boolean isSaved);
    }

    public RecipeAdapter(List<Recipe> recipes, OnBookmarkClickListener bookmarkClickListener, Context context, boolean isHomeActivity) {
        this.recipes = recipes;
        this.bookmarkClickListener = bookmarkClickListener;
        this.savedRecipeIds = new HashSet<>();
        this.context = context;
        this.isHomeActivity = isHomeActivity;
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
            private final ImageView recipeImage;
            private FrameLayout container;

            RecipeViewHolder(View itemView) {
                super(itemView);
                recipeTitle = itemView.findViewById(R.id.titleTextView);
                bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
                recipeImage = itemView.findViewById(R.id.recipeImageView); // Associa l'ImageView
                container = itemView.findViewById(R.id.cart_icon_container);
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

                // Carica l'immagine usando Glide
                Glide.with(context)
                        .load(recipe.getImage()) // Assicurati che il modello Recipe abbia un metodo getImageUrl()
                        .error(R.drawable.baseline_error_24) // Immagine mostrata in caso di errore
                        .into(recipeImage);

                if (isHomeActivity) {
                    container.setVisibility(View.GONE);
                }
                boolean isCurrentlySaved = savedRecipeIds.contains(recipe.getId());
                bookmarkButton.setImageResource(isCurrentlySaved ?
                        R.drawable.baseline_bookmark_24 :
                        R.drawable.baseline_bookmark_border_24);

                bookmarkButton.setOnClickListener(v -> {
                    boolean newSavedState;

                    if (savedRecipeIds.contains(recipe.getId())) {
                        savedRecipeIds.remove(recipe.getId());
                        newSavedState = false;
                    } else {
                        savedRecipeIds.add(recipe.getId());
                        newSavedState = true;
                    }

                    bookmarkClickListener.onBookmarkClick(recipe, newSavedState);

                    bookmarkButton.setImageResource(newSavedState ?
                            R.drawable.baseline_bookmark_24 :
                            R.drawable.baseline_bookmark_border_24);
                });
            }
        }

    }