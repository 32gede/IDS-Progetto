package com.example.progetto.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.progetto.R;
import com.example.progetto.data.model.UserProductUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.UserProductViewHolder> {

    private final Context context;
    private final List<UserProductUtils> userProducts;
    private final OnUserProductSelectedListener listener;
    private final OnRemoveButtonClickListener removeListener;

    // Listener per il pulsante di rimozione
    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(UserProductUtils userProduct);
    }

    // Listener per selezione normale
    public interface OnUserProductSelectedListener {
        void onUserProductSelected(UserProductUtils userProduct);
    }

    // Modifica il costruttore per accettare entrambi i listener
    public UserProductAdapter(Context context, List<UserProductUtils> userProducts,
                              OnUserProductSelectedListener listener,
                              OnRemoveButtonClickListener removeListener) {
        this.context = context;
        this.userProducts = new ArrayList<>(userProducts);
        this.listener = listener;
        this.removeListener = removeListener;
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

        // Set product data
        holder.productName.setText(userProduct.getName());
        holder.expiryDate.setText(userProduct.getExpiryDate());
        holder.quantity.setText(String.valueOf(userProduct.getQuantity()));

        // Change border color based on expiry date
        if (isExpired(userProduct.getExpiryDate())) {
            changeBorderColor(holder.cardView, R.color.selected_nav_color); // Red border
        }
        else if(staScadendo(userProduct.getExpiryDate())) {
            changeBorderColor(holder.cardView, R.color.yellow); // Yellow border

        }
        else {
            changeBorderColor(holder.cardView, R.color.gray); // Gray border
        }

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

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserProductSelected(userProduct);
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemoveButtonClick(userProduct);
            }
        });
    }

    private boolean staScadendo(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();
            long diff = expiry.getTime() - today.getTime();
            long days = diff / (24 * 60 * 60 * 1000);
            return days <= 7;
        } catch (ParseException e) {
            Log.e("UserProductAdapter", "Invalid date format: " + expiryDate, e);
            return false; // Tratta come non scaduto in caso di errore
        }
    }

    private void changeBorderColor(CardView cardView, int colorResId) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(12f); // Raggio degli angoli in dp
        drawable.setStroke(4, ContextCompat.getColor(context, colorResId)); // Colore del bordo
        drawable.setColor(ContextCompat.getColor(context, R.color.accent_light_purple)); // Sfondo trasparente
        cardView.setBackground(drawable); // Applica il drawable dinamico come background
    }


    @Override
    public int getItemCount() {
        return userProducts.size();
    }

    public void updateProductList(List<UserProductUtils> newProducts) {
        userProducts.clear();
        userProducts.addAll(newProducts);
        notifyDataSetChanged();
    }

    public static class UserProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, expiryDate, quantity;
        FloatingActionButton removeButton;
        CardView cardView; // Riferimento alla CardView

        public UserProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view); // Collegamento alla CardView
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            expiryDate = itemView.findViewById(R.id.expiry_date);
            quantity = itemView.findViewById(R.id.quantity);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }


    private boolean isExpired(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();
            return expiry != null && expiry.before(today);
        } catch (ParseException e) {
            Log.e("UserProductAdapter", "Invalid date format: " + expiryDate, e);
            return false; // Tratta come non scaduto in caso di errore
        }
    }
}
