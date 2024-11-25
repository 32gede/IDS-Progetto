package com.example.progetto.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.StoreUtils;
import com.example.progetto.ui.store.storeActivity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private List<StoreUtils> stores;
    private Set<String> savedStoreIds;
    private final OnBookmarkClickListener bookmarkClickListener;
    private final Context context;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(StoreUtils store, boolean isSaved);
    }

    public StoreAdapter(List<StoreUtils> stores, OnBookmarkClickListener bookmarkClickListener, Context context) {
        this.stores = stores;
        this.bookmarkClickListener = bookmarkClickListener;
        this.savedStoreIds = new HashSet<>();
        this.context = context;
    }

    public void setStores(List<StoreUtils> stores) {
        this.stores = stores;
        notifyDataSetChanged();
    }

    public void setSavedStoreIds(Set<String> savedStoreIds) {
        this.savedStoreIds = savedStoreIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_card, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreUtils store = stores.get(position);
        holder.bind(store, savedStoreIds.contains(store.getId()));
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView storeName;
        private final TextView storeDescription;
        private final TextView storePrice;
        private final ImageButton bookmarkButton;
        private final ImageView storeImage;

        StoreViewHolder(View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.storeNameTextView);
            storeDescription = itemView.findViewById(R.id.storeDescriptionTextView);
            storePrice = itemView.findViewById(R.id.storePriceTextView);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            storeImage = itemView.findViewById(R.id.storeImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    StoreUtils store = stores.get(position);
                    Intent intent = new Intent(context, storeActivity.class);
                    intent.putExtra("store", store);
                    context.startActivity(intent);
                }
            });
        }

        void bind(StoreUtils store, boolean isSaved) {
            storeName.setText(store.getName());
            storeDescription.setText(store.getDescription());
            storePrice.setText(store.getPrice());

            // Load the image using Glide
            Glide.with(context)
                    .load(store.getImage())
                    .error(R.drawable.baseline_error_24)
                    .into(storeImage);

            boolean isCurrentlySaved = savedStoreIds.contains(store.getId());
            bookmarkButton.setImageResource(isCurrentlySaved ?
                    R.drawable.baseline_bookmark_24 :
                    R.drawable.baseline_bookmark_border_24);

            bookmarkButton.setOnClickListener(v -> {
                boolean newSavedState;

                if (savedStoreIds.contains(store.getId())) {
                    savedStoreIds.remove(store.getId());
                    newSavedState = false;
                } else {
                    savedStoreIds.add(store.getId());
                    newSavedState = true;
                }

                bookmarkClickListener.onBookmarkClick(store, newSavedState);

                bookmarkButton.setImageResource(newSavedState ?
                        R.drawable.baseline_bookmark_24 :
                        R.drawable.baseline_bookmark_border_24);
            });
        }
    }
}