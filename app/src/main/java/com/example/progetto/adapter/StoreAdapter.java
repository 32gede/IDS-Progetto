// StoreAdapter.java
package com.example.progetto.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.StoreUtils;
import com.example.progetto.ui.store.StoreActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private static final String TAG = "StoreAdapter";
    private List<StoreUtils> stores;
    private Set<String> savedStoreIds;
    private final Context context;
    private int selectedTabPosition;

    public StoreAdapter(List<StoreUtils> stores, Context context, int selectedTabPosition) {
        this.stores = stores;
        this.savedStoreIds = new HashSet<>();
        this.context = context;
        this.selectedTabPosition = selectedTabPosition;
    }

    public void setStores(List<StoreUtils> stores) {
        if (stores != null) {
            this.stores = stores;
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "setStores called with null list");
        }
    }

    public void setSavedStoreIds(Set<String> savedStoreIds) {
        if (savedStoreIds != null) {
            this.savedStoreIds = savedStoreIds;
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "setSavedStoreIds called with null set");
        }
    }

    public void setSelectedTabPosition(int selectedTabPosition) {
        this.selectedTabPosition = selectedTabPosition;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.store_card, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        if (stores != null && position < stores.size()) {
            StoreUtils store = stores.get(position);
            boolean isSaved = savedStoreIds.contains(store.getId());
            holder.bind(store, isSaved, selectedTabPosition);
        } else {
            Log.e(TAG, "Invalid position or empty store list in onBindViewHolder");
        }
    }

    @Override
    public int getItemCount() {
        return (stores != null) ? stores.size() : 0;
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView storeName;
        private final TextView storeDescription;
        private final TextView storePrice;
        private final ImageView storeImage;
        private final ImageView actionIcon;

        StoreViewHolder(View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.storeNameTextView);
            storeDescription = itemView.findViewById(R.id.storeDescriptionTextView);
            storePrice = itemView.findViewById(R.id.storePriceTextView);
            storeImage = itemView.findViewById(R.id.storeImageView);
            actionIcon = itemView.findViewById(R.id.cart_icon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && stores != null && position < stores.size()) {
                    StoreUtils store = stores.get(position);
                    Intent intent = new Intent(context, StoreActivity.class);
                    intent.putExtra("store", store);
                    context.startActivity(intent);
                } else {
                    Log.e(TAG, "Invalid position or empty store list in onClickListener");
                }
            });
        }

        void bind(StoreUtils store, boolean isSaved, int selectedTabPosition) {
            if (store != null) {
                storeName.setText(store.getName());
                storeDescription.setText(store.getDescription());
                storePrice.setText(store.getPrice());

                Glide.with(context)
                        .load(store.getImage())
                        .error(R.drawable.baseline_error_24)
                        .into(storeImage);

                if (selectedTabPosition == 0) {
                    actionIcon.setImageResource(R.drawable.baseline_edit_24);
                } else {
                    actionIcon.setImageResource(R.drawable.shopping_cart_svgrepo_com);
                }
            } else {
                Log.e(TAG, "Store object is null in bind method");
            }
        }
    }
}