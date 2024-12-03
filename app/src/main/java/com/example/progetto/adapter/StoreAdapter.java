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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.progetto.R;
import com.example.progetto.data.model.StoreUtils;
import com.example.progetto.ui.store.StoreActivity;
import com.example.progetto.ui.store.StoreFocusActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private static final String TAG = "StoreAdapter";
    private List<StoreUtils> stores;
    private Set<String> savedStoreIds;
    private final Context context;
    private int selectedTabPosition;
    private boolean isCartActivity;

    public StoreAdapter(List<StoreUtils> stores, Context context, int selectedTabPosition, boolean isCartActivity) {
        this.stores = stores;
        this.savedStoreIds = new HashSet<>();
        this.context = context;
        this.selectedTabPosition = selectedTabPosition;
        this.isCartActivity = isCartActivity;
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
                    Intent intent = new Intent(context, StoreFocusActivity.class);
                    intent.putExtra("store", store);
                    context.startActivity(intent);
                } else {
                    Log.e(TAG, "Invalid position or empty store list in onClickListener");
                }
            });

            actionIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && stores != null && position < stores.size()) {
                    StoreUtils store = stores.get(position);
                    if (selectedTabPosition == 1 && !isCartActivity) { // Global tab
                        addToUserStore(store);
                    }
                    if (isCartActivity) {
                        removeFromUserStore(store);
                    }
                } else {
                    Log.e(TAG, "Invalid position or empty store list in actionIcon onClickListener");
                }
            });
        }

        private void removeFromUserStore(StoreUtils store) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String currentUserId = mAuth.getCurrentUser().getUid();
            String storeId = store.getId();
            db.collection("user_store")
                    .whereEqualTo("buyer_id", currentUserId)
                    .whereEqualTo("store_id", storeId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (int i = 0; i < task.getResult().size(); i++) {
                                db.collection("user_store")
                                        .document(task.getResult().getDocuments().get(i).getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Store removed from user_store");
                                            Toast.makeText(context, "Box rimosso dal carrello", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Error removing store from user_store", e));
                            }
                        } else {
                            Log.e(TAG, "Error getting user stores for checkout: ", task.getException());
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

                if (isCartActivity) {
                    actionIcon.setImageResource(R.drawable.baseline_remove_shopping_cart_24);
                } else if (selectedTabPosition == 1) {
                    actionIcon.setImageResource(R.drawable.shopping_cart_svgrepo_com);
                } else {
                    actionIcon.setImageResource(R.drawable.baseline_edit_24);
                }
            } else {
                Log.e(TAG, "Store object is null in bind method");
            }
        }

        private void addToUserStore(StoreUtils store) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String currentUserId = mAuth.getCurrentUser().getUid();

            db.collection("user_store")
                    .whereEqualTo("buyer_id", currentUserId)
                    .whereEqualTo("store_id", store.getId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                            db.collection("user_store")
                                    .add(new UserStore(currentUserId, store.getId()))
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "Store added to user_store with ID: " + documentReference.getId());
                                        Toast.makeText(context, "Box aggiunto nel carrello", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error adding store to user_store", e));
                        } else {
                            Log.d(TAG, "Store already exists in user_store");
                            Toast.makeText(context, "Box già nel carrello", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error checking user_store", e));
        }
    }

    static class UserStore {
        private String buyer_id;
        private String store_id;

        public UserStore(String buyer_id, String store_id) {
            this.buyer_id = buyer_id;
            this.store_id = store_id;
        }

        public String getBuyer_id() {
            return buyer_id;
        }

        public void setBuyer_id(String buyer_id) {
            this.buyer_id = buyer_id;
        }

        public String getStore_id() {
            return store_id;
        }

        public void setStore_id(String store_id) {
            this.store_id = store_id;
        }
    }
}