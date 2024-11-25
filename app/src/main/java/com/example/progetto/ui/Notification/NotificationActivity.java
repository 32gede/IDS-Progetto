package com.example.progetto.ui.Notification;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.progetto.R;
import com.example.progetto.adapter.NotificationAdapter;
import com.example.progetto.adapter.RecipeAdapter;
import com.example.progetto.data.model.NotificationItem;
import com.example.progetto.data.model.UserProductUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private ImageView backButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerView);
        backButton = findViewById(R.id.back_button);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher());


        // Inizializza la lista di notifiche
        notificationList = new ArrayList<>();
        notificationList.add(new NotificationItem("Benvenuto!", "Grazie per aver scaricato la nostra app.", "10:30 AM"));
        notificationList.add(new NotificationItem("Offerta Speciale", "Sconto del 20% sul primo acquisto!", "11:00 AM"));
        notificationList.add(new NotificationItem("Promemoria", "Hai un appuntamento domani alle 15:00.", "9:00 PM"));

        // Imposta l'adapter
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
    }
//    private void loadItemsFromFirestore() {
//        // Start refreshing animation if not already started
//        swipeRefreshLayout.setRefreshing(true);
//
//        // Retrieve user ID to filter specific products
//        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
//        Log.d("NotificationActivity", "User ID: " + userId);
//
//        if (userId == null) {
//            Log.e("NotificationActivity", "User ID is null. Cannot load items.");
//            swipeRefreshLayout.setRefreshing(false);
//            return;
//        }
//
//        // Query to "user_products" collection to get only products associated with the user
//        firestore.collection("Notification")
//                .whereEqualTo("userId", userId)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    fridgeProductList.clear(); // Clear current product list
//
//                    // Populate the list with UserProductUtils objects
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        UserProductUtils userProduct = document.toObject(UserProductUtils.class);
//                        fridgeProductList.add(userProduct);
//                    }
//
//                    // Update the filtered list and RecyclerView
//                    filteredList.clear();
//                    filteredList.addAll(fridgeProductList);
//                    productAdapter.updateProductList(filteredList);
//                    Log.d("FridgeActivity", "Items loaded successfully from Firestore. Total: " + fridgeProductList.size());
//
//                    // Stop the refreshing animation
//                    swipeRefreshLayout.setRefreshing(false);
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("FridgeActivity", "Failed to load user products: " + e.getMessage());
//                    swipeRefreshLayout.setRefreshing(false); // Stop refreshing animation on failure
//                });
//    }
}

