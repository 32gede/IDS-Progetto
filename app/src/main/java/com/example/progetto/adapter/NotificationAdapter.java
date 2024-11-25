package com.example.progetto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.data.model.ItemUtils;
import com.example.progetto.data.model.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;

    // Costruttore
    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        holder.title.setText(notification.getName());
        holder.message.setText(notification.getUserId());
        holder.timestamp.setText(notification.getExpiryDate());
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

    // ViewHolder interno
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, timestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            message = itemView.findViewById(R.id.notification_message);
            timestamp = itemView.findViewById(R.id.notification_timestamp);
        }
    }
}
