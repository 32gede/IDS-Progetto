package com.example.progetto.ui.recipe;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.progetto.R;
import com.example.progetto.data.model.Recipe;

public class RecipeFocusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_focus);

        TextView titleTextView = findViewById(R.id.recipe_title);
        TextView difficultyTextView = findViewById(R.id.recipe_difficulty);
        TextView categoryTextView = findViewById(R.id.recipe_category);
        TextView preparationTimeTextView = findViewById(R.id.recipe_preparation_time);
        TextView ingredientsTextView = findViewById(R.id.recipe_ingredients);
        TextView stepsTextView = findViewById(R.id.recipe_steps);
        TextView descriptionTextView = findViewById(R.id.recipe_description);

        // Get the recipe passed from the previous activity
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe != null) {
            titleTextView.setText(recipe.getName());
            descriptionTextView.setText(recipe.getDescription());
            difficultyTextView.setText(recipe.getDifficulty());
            categoryTextView.setText(recipe.getCategory());
            preparationTimeTextView.setText(recipe.getPreparationTime());
            ingredientsTextView.setText(recipe.getIngredients());
            stepsTextView.setText(recipe.getSteps());
            // Set other recipe details as needed
        }
    }
}