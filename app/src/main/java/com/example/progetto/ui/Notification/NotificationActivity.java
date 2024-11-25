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
import com.example.progetto.data.model.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity"; // Tag per i log

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private List<NotificationItem> notificationProductList = new ArrayList<>();
    private ImageView backButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Inizializza Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        // Configura SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "User triggered a refresh.");
            loadItemsFromFirestore();
        });

        // Carica notifiche iniziali
        Log.d(TAG, "Loading initial notifications from Firestore.");
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

        // Query Firestore
        firestore.collection("Notification")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationProductList.clear(); // Pulisce la lista corrente
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "No notifications found for the user.");
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                NotificationItem notification = document.toObject(NotificationItem.class);
                                notificationProductList.add(notification);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document to NotificationItem: " + document.getId(), e);
                            }
                        }
                    }

                    // Aggiorna l'adapter
                    adapter.updateNotificationList(notificationProductList);

                    Log.d(TAG, "Items loaded successfully from Firestore. Total: " + notificationProductList.size());
                    swipeRefreshLayout.setRefreshing(false); // Ferma l'animazione
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user notifications: " + e.getMessage(), e);
                    swipeRefreshLayout.setRefreshing(false); // Ferma l'animazione
                });
    }
}
