package com.example.progetto.ui.Notification;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progetto.R;
import com.example.progetto.adapter.NotificationAdapter;
import com.example.progetto.data.model.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private ImageView backButton;

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
}

