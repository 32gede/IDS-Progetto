package com.example.progetto.data.model;

import android.app.Activity;
import android.content.Intent;

import com.example.progetto.R;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.store.StoreActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationHelper {

    /**
     * Imposta il listener per la navigazione nel BottomNavigationView.
     *
     * @param activity            L'activity corrente
     * @param bottomNavigationView Il BottomNavigationView da configurare
     */
    public static void setupNavigation(Activity activity, BottomNavigationView bottomNavigationView) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.home_button) {
                    navigateTo(activity, HomeActivity.class);
                    return true;
                }
                if (itemId == R.id.store_button) {
                    navigateTo(activity, StoreActivity.class);
                    return true;
                }
                if (itemId == R.id.fridge_button) {
                    navigateTo(activity, FridgeActivity.class);
                    return true;
                }
                if (itemId == R.id.recipe_button) {
                    navigateTo(activity, RecipeActivity.class);
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Naviga all'activity target senza animazioni di transizione.
     *
     * @param currentActivity L'activity corrente
     * @param targetActivity  L'activity di destinazione
     */
    private static void navigateTo(Activity currentActivity, Class<?> targetActivity) {
        if (!currentActivity.getClass().equals(targetActivity)) {
            Intent intent = new Intent(currentActivity, targetActivity);
            currentActivity.startActivity(intent);
            currentActivity.overridePendingTransition(0, 0); // Disabilita transizioni
            currentActivity.finish();
        }
    }
}
