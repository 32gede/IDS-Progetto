// src/main/java/com/example/progetto/utils/NotificationScheduler.java
package com.example.progetto.data.model.Note;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleExpiryNotification(Context context, String productName, Calendar expiryDate) {
        Calendar notificationTime = (Calendar) expiryDate.clone();
        notificationTime.add(Calendar.DAY_OF_YEAR, -7); // Set notification time to one week before expiry

        long delay = notificationTime.getTimeInMillis() - System.currentTimeMillis();

        Data inputData = new Data.Builder()
                .putString("productName", productName)
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(ExpiryNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueue(notificationWork);
    }
}