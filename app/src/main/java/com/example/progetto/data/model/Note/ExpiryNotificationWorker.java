package com.example.progetto.data.model.Note;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ExpiryNotificationWorker extends Worker {

    private static final String TAG = "ExpiryNotificationWorker";

    public ExpiryNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String productName = getInputData().getString("productName");
        int quantity = getInputData().getInt("quantity", 0);
        Log.d(TAG, "doWork: productName = " + productName + ", quantity = " + quantity);
        showNotification(productName, quantity);
        return Result.success();
    }

    private void showNotification(String productName, int quantity) {
        String channelId = "expiry_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notifiche Scadenza",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        String appo;
        if (quantity > 1) {
            appo = "Il prodotto \"" + productName + "\" scade domani.";
        } else {
            appo = quantity + " prodotti di " + productName + "\" scadono domani.";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Scadenza in arrivo!")
                .setContentText(appo)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Log.d(TAG, "showNotification: Creating notification for productName = " + productName);
        notificationManager.notify(productName.hashCode(), builder.build());
        saveProductDataToFirestore(productName);
    }

    private void saveProductDataToFirestore(String productName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", productName);

        firestore.collection("Notification")
                .add(productData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "saveProductDataToFirestore: Successfully added product data to Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "saveProductDataToFirestore: Failed to add product data to Firestore", e);
                });
    }
}