package com.example.progetto.data.model;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;

import com.example.progetto.R;
import com.example.progetto.ui.Notification.NotificationActivity;
import com.example.progetto.ui.fridge.FridgeActivity;
import com.example.progetto.ui.home.HomeActivity;
import com.example.progetto.ui.profile.ProfileActivity;
import com.example.progetto.ui.recipe.RecipeActivity;
import com.example.progetto.ui.store.StoreActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    /**
     * Sets up navigation for the BottomNavigationView.
     *
     * @param activity             The current activity.
     * @param bottomNavigationView The BottomNavigationView to configure.
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
     * Sets up click listeners for toolbar buttons.
     *
     * @param profileButtonTop     The profile button.
     * @param notificationButtonTop The notification button.
     * @param activity             The current activity.
     */
    public static void setupToolbar(ImageButton profileButtonTop, ImageButton notificationButtonTop, Activity activity) {
        if (profileButtonTop != null) {
            profileButtonTop.setOnClickListener(v -> navigateTo(activity, ProfileActivity.class));
        }

        if (notificationButtonTop != null) {
            notificationButtonTop.setOnClickListener(v -> navigateTo(activity, NotificationActivity.class));
        }
    }

    /**
     * Navigates to the target activity.
     *
     * @param currentActivity The current activity.
     * @param targetActivity  The activity to navigate to.
     */
    private static void navigateTo(Activity currentActivity, Class<?> targetActivity) {
        if (!currentActivity.getClass().equals(targetActivity)) {
            Intent intent = new Intent(currentActivity, targetActivity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            currentActivity.startActivity(intent);
            currentActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
