package com.example.progetto.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.progetto.fragment.globalRecipe;
import com.example.progetto.fragment.savedRecipe;

public class RecipePagerAdapter extends FragmentStateAdapter{

    public RecipePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new savedRecipe(); // Primo tab
            case 1:
                return new globalRecipe(); // Secondo tab
            default:
                return new savedRecipe(); // Terzo tab
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Numero di tab
    }
}

