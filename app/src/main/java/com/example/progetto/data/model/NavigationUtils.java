    package com.example.progetto.data.model;

    import android.view.View;

    import com.example.progetto.R;

    public class NavigationUtils {

        public static void updateNavSelection(int selectedButtonId, View homeCircle, View searchCircle, View fridgeCircle, View recipeCircle) {
            setAllCirclesInvisible(homeCircle, searchCircle, fridgeCircle, recipeCircle);

            if (selectedButtonId == R.id.homeButton && homeCircle != null) {
                homeCircle.setVisibility(View.VISIBLE);
            } else if (selectedButtonId == R.id.fridgeButton && fridgeCircle != null) {
                fridgeCircle.setVisibility(View.VISIBLE);
            } else if (selectedButtonId == R.id.storeButton && searchCircle != null) {
                searchCircle.setVisibility(View.VISIBLE);
            } else if (selectedButtonId == R.id.recipeButton && recipeCircle != null) {
                recipeCircle.setVisibility(View.VISIBLE);
            }
        }

        private static void setAllCirclesInvisible(View homeCircle, View searchCircle, View fridgeCircle, View recipeCircle) {
            if (homeCircle != null) homeCircle.setVisibility(View.GONE);
            if (searchCircle != null) searchCircle.setVisibility(View.GONE);
            if (fridgeCircle != null) fridgeCircle.setVisibility(View.GONE);
            if (recipeCircle != null) recipeCircle.setVisibility(View.GONE);
        }
    }
