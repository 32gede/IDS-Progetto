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
        TextView descriptionTextView = findViewById(R.id.recipe_description);

        // Get the recipe passed from the previous activity
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe != null) {
            titleTextView.setText(recipe.getName());
            descriptionTextView.setText(recipe.getDescription());
            // Set other recipe details as needed
        }
    }
}