package com.example.progetto.adapter;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.data.model.SelectedIngredientUtils;

import java.util.ArrayList;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private final List<ItemUtils> ingredientsList;
    private final List<SelectedIngredientUtils> selectedIngredients;

    public IngredientsAdapter(List<ItemUtils> ingredients) {
        this.ingredientsList = ingredients;
        this.selectedIngredients = new ArrayList<>();
    }

    public List<SelectedIngredientUtils> getSelectedIngredients() {
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

        // Set ingredient name
        holder.ingredientName.setText(ingredient.getName());

        // Check if ingredient is already selected
        SelectedIngredientUtils existingSelection = findSelectedIngredientByName(ingredient.getProductId());
        boolean isSelected = existingSelection != null;

        // Update UI based on selection
        holder.itemView.setSelected(isSelected);
        holder.ingredientName.setTextColor(isSelected ? Color.MAGENTA : Color.BLACK);
        holder.ingredientQuantity.setEnabled(isSelected);

        if (isSelected) {
            holder.ingredientQuantity.setText(String.valueOf(existingSelection.getQuantity()));
        } else {
            holder.ingredientQuantity.setText(""); // Reset quantity for non-selected items
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                // Remove from selected
                selectedIngredients.remove(existingSelection);
            } else {
                // Add to selected
                selectedIngredients.add(new SelectedIngredientUtils(ingredient.getProductId(), parseQuantity(holder.ingredientQuantity.getText().toString())));
            }
            notifyItemChanged(position);
        });

        // Update quantity in real-time
        holder.ingredientQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isSelected) {
                    int quantity = parseQuantity(s.toString());
                    existingSelection.setQuantity(quantity);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredientsList.size();
    }

    // Helper method to parse quantity safely
    private int parseQuantity(String quantityStr) {
        try {
            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Helper method to find a SelectedIngredient object by name
    private SelectedIngredientUtils findSelectedIngredientByName(String name) {
        for (SelectedIngredientUtils selected : selectedIngredients) {
            if (selected.getName().equals(name)) {
                return selected;
            }
        }
        return null;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView ingredientName;
        public final EditText ingredientQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
        }
    }
}

// SelectedIngredient class

