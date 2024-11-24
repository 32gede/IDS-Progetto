package com.example.progetto.data.model.Note;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ExpiryNotificationWorker extends Worker {

    public ExpiryNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String productName = getInputData().getString("productName");
        showNotification(productName);
        return Result.success();
    }

    private void showNotification(String productName) {
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Scadenza in arrivo!")
                .setContentText("Il prodotto \"" + productName + "\" scade domani.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        saveProductDataToFirestore(productName);
        notificationManager.notify(productName.hashCode(), builder.build());
    }

    private void saveProductDataToFirestore(String productName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", productName);

        firestore.collection("Notification")
                .add(productData)
                .addOnSuccessListener(documentReference -> {
                    // Log success or perform additional actions if needed
                })
                .addOnFailureListener(e -> {
                    // Log failure or perform additional actions if needed
                });
    }
}