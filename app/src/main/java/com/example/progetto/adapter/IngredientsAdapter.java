package com.example.progetto.adapter;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private final List<ItemUtils> ingredientsList;
    private final Map<String, SelectedIngredientUtils> selectedIngredients;

    public IngredientsAdapter(List<ItemUtils> ingredients) {
        this.ingredientsList = ingredients;
        this.selectedIngredients = new HashMap<>();
    }

    public List<SelectedIngredientUtils> getSelectedIngredients() {
        return new ArrayList<>(selectedIngredients.values());
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

        // Check if ingredient is selected
        SelectedIngredientUtils existingSelection = selectedIngredients.get(ingredient.getName());
        boolean isSelected = existingSelection != null;

        // Update UI based on selection
        holder.itemView.setSelected(isSelected);
        holder.ingredientName.setTextColor(isSelected ? Color.MAGENTA : Color.BLACK);
        holder.ingredientQuantity.setEnabled(isSelected);

        // Avoid re-adding TextWatchers during recycling
        holder.ingredientQuantity.removeTextChangedListener(holder.textWatcher);

        // Set quantity if selected
        if (isSelected) {
            holder.ingredientQuantity.setText(String.valueOf(existingSelection.getQuantity()));
        } else {
            holder.ingredientQuantity.setText(""); // Reset quantity for non-selected items
        }

        // Add TextWatcher to update quantity in real-time
        holder.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isSelected) {
                    int quantity = parseQuantity(s.toString());
                    if (existingSelection != null) {
                        existingSelection.setQuantity(quantity);
                        selectedIngredients.put(ingredient.getName(), existingSelection);
                        Log.d("IngredientsAdapter", "Updated ingredient quantity: " + quantity);
                    }
                }
            }
        };
        holder.ingredientQuantity.addTextChangedListener(holder.textWatcher);

        // Handle item click to select or deselect ingredients
        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                // Remove from selected
                selectedIngredients.remove(ingredient.getName());
            } else {
                // Add to selected
                int quantity = parseQuantity(holder.ingredientQuantity.getText().toString());
                SelectedIngredientUtils newSelection = new SelectedIngredientUtils(ingredient.getName(), quantity);
                selectedIngredients.put(ingredient.getName(), newSelection);
            }

            // Notify adapter to refresh UI
            notifyItemChanged(position);
            Log.d("IngredientsAdapter", "Selected ingredients: " + selectedIngredients.size());
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

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView ingredientName;
        public final EditText ingredientQuantity;
        public TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
        }
    }
}