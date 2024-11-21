package com.example.progetto.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.data.model.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private final List<ItemUtils> ingredientsList;
    private final List<ItemUtils> selectedIngredients;

    public IngredientsAdapter(List<ItemUtils> ingredients) {
        this.ingredientsList = ingredients;
        this.selectedIngredients = new ArrayList<>();
    }

    public List<ItemUtils> getSelectedIngredients() {
        return selectedIngredients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemUtils ingredient = ingredientsList.get(position);
        holder.ingredientName.setText(ingredient.getName());

        // Aggiorna lo stato di selezione
        boolean isSelected = selectedIngredients.contains(ingredient);
        holder.itemView.setSelected(isSelected);

        // Cambia il colore del testo (opzionale)
        holder.ingredientName.setTextColor(isSelected ? Color.WHITE : Color.BLACK);

        // Listener per gestire il click sull'intera card
        holder.itemView.setOnClickListener(v -> {
            if (selectedIngredients.contains(ingredient)) {
                selectedIngredients.remove(ingredient);
            } else {
                selectedIngredients.add(ingredient);
            }
            notifyItemChanged(position); // Aggiorna l'elemento cliccato
        });
    }

    @Override
    public int getItemCount() {
        return ingredientsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView ingredientName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
        }
    }
}
