package com.example.progetto.data.model.Note;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ExpiryNotificationWorker extends Worker {

    private static final String TAG = "ExpiryNotificationWorker";
    private static final String CHANNEL_ID = "expiry_notification_channel";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public ExpiryNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Recupera i dati passati al Worker
        String productName = getInputData().getString("productName");
        int quantity = getInputData().getInt("quantity", 0);
        String expiryDate = getInputData().getString("expiryDate");

        if (productName == null || productName.isEmpty()) {
            Log.e(TAG, "doWork: productName è nullo o vuoto");
            return Result.failure();
        }

        Log.d(TAG, "doWork: productName = " + productName + ", quantity = " + quantity);

        // Mostra la notifica e salva i dati
        showNotification(productName, quantity,expiryDate);
        saveProductDataToFirestore(productName,quantity,expiryDate);

        return Result.success();
    }

    private void showNotification(String productName, int quantity,String ex) {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Crea il canale di notifica se necessario (solo Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notifiche Scadenza",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifiche per prodotti in scadenza");
            notificationManager.createNotificationChannel(channel);
        }

        // Costruisci il testo della notifica
        String notificationText = quantity > 1
                ? quantity + " prodotti di \"" + productName + "\" scadono \""+ex+"\"."
                : "Il prodotto \"" + productName + "\" scade "+ex+".";

        // Costruisci la notifica
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Scadenza in arrivo!")
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mostra la notifica
        if (notificationManager != null) {
            Log.d(TAG, "showNotification: Creazione notifica per productName = " + productName);
            notificationManager.notify(productName.hashCode(), builder.build());
        } else {
            Log.e(TAG, "showNotification: NotificationManager è nullo");
        }
    }

    private void saveProductDataToFirestore(String productName, int quantity, String expiryDate) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Prepara i dati da salvare
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", productName);
        productData.put("quantity", quantity);
        productData.put("expiryDate", expiryDate);
        productData.put("userId", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null);

        // Aggiungi i dati alla collezione "Notification" di Firestore
        firestore.collection("Notification")
                .add(productData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "saveProductDataToFirestore: Dati salvati con successo su Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "saveProductDataToFirestore: Errore nel salvataggio su Firestore", e);
                });
    }

}