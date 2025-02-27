package com.example.progetto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.data.model.NotificationItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private final OnRemoveButtonClickListener removeListener;

    public interface OnRemoveButtonClickListener {
        void onRemoveButtonClick(NotificationItem notification);
    }

    // Constructor
    public NotificationAdapter(List<NotificationItem> notificationList, OnRemoveButtonClickListener removeListener) {
        this.notificationList = notificationList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view, removeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        String notificationText = notification.getQuantity() > 1
                ? notification.getQuantity() + " prodotti di \"" + notification.getProductName() + "\" scadono \"" + notification.getExpiryDate() + "\"."
                : "Il prodotto \"" + notification.getProductName() + "\" scade " + notification.getExpiryDate() + ".";
        holder.title.setText(notification.getProductName());
        holder.message.setText(notificationText);
        holder.timestamp.setText(notification.getExpiryDate());
        holder.removeButton.setTag(notification); // Set the notification as the tag
    }

    public void updateNotificationList(List<NotificationItem> newProducts) {
        notificationList.clear();
        notificationList.addAll(newProducts);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Inner ViewHolder class
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, timestamp;
        FloatingActionButton removeButton;

        public NotificationViewHolder(@NonNull View itemView, OnRemoveButtonClickListener removeListener) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            message = itemView.findViewById(R.id.notification_message);
            timestamp = itemView.findViewById(R.id.notification_timestamp);
            removeButton = itemView.findViewById(R.id.remove_button);

            removeButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    NotificationItem notification = (NotificationItem) v.getTag();
                    removeListener.onRemoveButtonClick(notification);
                }
            });
        }
    }
}