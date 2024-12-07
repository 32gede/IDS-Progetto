package com.example.progetto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.data.model.SelectedIngredientUtils;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {

    private List<SelectedIngredientUtils> ingredients;
    private final Context context;

    public IngredientsAdapter(List<SelectedIngredientUtils> ingredients, Context context) {
        this.ingredients = ingredients;
        this.context = context;
    }

    public void updateData(List<SelectedIngredientUtils> newIngredients) {
        this.ingredients = newIngredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        SelectedIngredientUtils ingredient = ingredients.get(position);

        // Bind ingredient data
        holder.ingredientName.setText(ingredient.getName());
        holder.ingredientQuantity.setText(String.valueOf(ingredient.getQuantity()));

    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName;
        EditText ingredientQuantity;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
        }
    }
}
