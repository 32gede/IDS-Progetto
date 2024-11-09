package com.example.progetto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.progetto.R;
import com.example.progetto.data.model.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<ItemUtils> products;

    public ProductAdapter(Context context, List<ItemUtils> products) {
        this.context = context;
        this.products = new ArrayList<>(products);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ItemUtils product = products.get(position);
        holder.productName.setText(product.getName());

        // Load image with Glide, applying a placeholder and error image in case of failure
//        Glide.with(context)
//                .load(product.getUrl())
//                .apply(new RequestOptions()
//                        .placeholder(R.drawable.placeholder_image)
//                        .error(R.drawable.error_image)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                )
//                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Method to update the product list dynamically
    public void updateProductList(List<ItemUtils> newProducts) {
        products.clear();
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
        }
    }
}
