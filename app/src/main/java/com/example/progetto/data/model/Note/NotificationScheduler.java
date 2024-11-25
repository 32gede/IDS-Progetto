package com.example.progetto.data.model.Note;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";

    public static void scheduleExpiryNotification(Context context, String productName, int quantity, Calendar expiryDate) {
        Calendar notificationTime = (Calendar) expiryDate.clone();
        notificationTime.add(Calendar.DAY_OF_YEAR, -7); // Set notification time to one week before expiry

        long delay = notificationTime.getTimeInMillis() - System.currentTimeMillis();

        if (delay <= 0) {
            Log.w(TAG, "scheduleExpiryNotification: Notification time is in the past. Showing notification immediately.");
            delay = 0;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String expiryDateString = dateFormat.format(expiryDate.getTime());

        Data inputData = new Data.Builder()
                .putString("productName", productName)
                .putInt("quantity", quantity)
                .putString("expiryDate", expiryDateString)
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(ExpiryNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        Log.d(TAG, "scheduleExpiryNotification: Scheduling notification for productName = " + productName + ", quantity = " + quantity + ", delay = " + delay);
        WorkManager.getInstance(context).enqueue(notificationWork);
    }
}