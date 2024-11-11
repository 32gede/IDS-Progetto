package com.example.progetto.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.progetto.R;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.data.model.UserProductUtils;

import java.util.ArrayList;
import java.util.List;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.UserProductViewHolder> {

    private final Context context;
    private final List<UserProductUtils> userProducts;
    private final OnUserProductSelectedListener listener;

    // Define the listener interface for item selection
    public interface OnUserProductSelectedListener {
        void onUserProductSelected(UserProductUtils userProduct);
    }

    // Modify constructor to accept the listener
    public UserProductAdapter(Context context, List<UserProductUtils> userProducts, OnUserProductSelectedListener listener) {
        this.context = context;
        this.userProducts = new ArrayList<>(userProducts);
        this.listener = listener;
    }
    public void updateProductList(List<UserProductUtils> newProducts) {
        userProducts.clear();
        userProducts.addAll(newProducts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_product_card, parent, false);
        return new UserProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserProductViewHolder holder, int position) {
        UserProductUtils userProduct = userProducts.get(position);

        // Set the product name and other fields
        holder.productName.setText(userProduct.getName());
        holder.expiryDate.setText(userProduct.getExpiryDate());
        holder.quantity.setText(String.valueOf(userProduct.getQuantity()));

        // Load product image with Glide
        Glide.with(context)
                .load(userProduct.getUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("Glide", "Error loading image", e);
                        return false; // Let Glide handle the error
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.productImage);

        // Set click listener on each item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserProductSelected(userProduct);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userProducts.size();
    }

    // Method to update the user product list dynamically
    public void updateUserProductList(List<UserProductUtils> newUserProducts) {
        userProducts.clear();
        userProducts.addAll(newUserProducts);
        notifyDataSetChanged();
    }

    public static class UserProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, expiryDate, quantity;

        public UserProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            expiryDate = itemView.findViewById(R.id.expiry_date);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }
}
