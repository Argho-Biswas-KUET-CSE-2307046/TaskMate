
package com.example.taskmate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String taskTitle = intent.getStringExtra("taskTitle");
        String taskDescription =
                intent.getStringExtra("taskDescription");

        if (taskTitle == null || taskTitle.trim().isEmpty()) {
            taskTitle = "Task Reminder";
        }

        if (taskDescription == null ||
                taskDescription.trim().isEmpty()) {
            taskDescription = "You have a task to complete.";
        }

        createNotificationChannel(context);

        Notification notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(taskTitle)
                        .setContentText(taskDescription)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(taskDescription)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .build();

        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager != null) {
            int notificationId = intent.getIntExtra(
                    "notificationId",
                    (int) (System.currentTimeMillis() % Integer.MAX_VALUE)
            );

            manager.notify(notificationId, notification);
        }
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(
                    "Notifications for upcoming tasks and deadlines."
            );

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
