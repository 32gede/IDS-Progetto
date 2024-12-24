package com.example.progetto.ui.Notification;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.NotificationAdapter;
import com.example.progetto.data.model.Firestore;
import com.example.progetto.data.model.FirestoreCallback;
import com.example.progetto.data.model.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity"; // Tag per i log

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private List<NotificationItem> notificationProductList = new ArrayList<>();
    private ImageView backButton;
    private Firestore firestore;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Inizializza Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = new Firestore();

        // Inizializza RecyclerView e SwipeRefreshLayout
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout); // Assicurati che esista nel layout XML
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inizializza pulsante di ritorno
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button pressed");
            onBackPressed();
        });

        // Inizializza lista e adapter
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, notification -> {
            Log.d(TAG, "Remove button clicked for notification: " + notification.getId());
            // Rimuovi la notifica
            firestore.removeNotification(notification.getId(), new FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Log.d(TAG, "Notification removed successfully: " + notification.getId());
                    notificationProductList.remove(notification);
                    adapter.updateNotificationList(notificationProductList);
                }


                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to remove notification: " + notification.getId(), e);
                }
            });
        });
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this::loadItemsFromFirestore);
        loadItemsFromFirestore();
    }

    private void loadItemsFromFirestore() {
        // Avvia l'animazione di refresh
        swipeRefreshLayout.setRefreshing(true);

        // Recupera l'ID dell'utente
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        Log.d(TAG, "User ID retrieved: " + userId);

        if (userId == null) {
            Log.e(TAG, "User ID is null. Cannot load items.");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        firestore.getNotification(userId, new FirestoreCallback<List<NotificationItem>>() {
            @Override
            public void onSuccess(List<NotificationItem> data) {
                notificationProductList.clear(); // Pulisce la lista corrente
                notificationProductList.addAll(data); // Aggiunge i nuovi dati
                adapter.updateNotificationList(notificationProductList); // Aggiorna l'adapter
                swipeRefreshLayout.setRefreshing(false); // Ferma l'animazione

                if (!notificationProductList.isEmpty()) {
                    Log.d(TAG, "First Item: " + notificationProductList.get(0).getId());
                } else {
                    Log.d(TAG, "No notifications found.");
                }

                Log.d(TAG, "Items loaded successfully from Firestore. Total: " + notificationProductList.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load notifications: " + e.getMessage(), e);
                swipeRefreshLayout.setRefreshing(false); // Ferma l'animazione
            }
        });
    }
}