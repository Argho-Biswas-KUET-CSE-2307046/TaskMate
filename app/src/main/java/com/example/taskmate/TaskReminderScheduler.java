
package com.example.taskmate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskReminderScheduler {

    private static final String ACTION_TASK_REMINDER =
            "com.example.taskmate.TASK_REMINDER";

    // Schedule a reminder for a task.
    public static void scheduleReminder(Context context, Task task) {

        if (task == null || task.getId() <= 0) {
            return;
        }

        // No reminder selected.
        if (task.getReminder() == null ||
                task.getReminder().equals("No reminder")) {

            cancelReminder(context, task.getId());
            return;
        }

        // Date and time are both required.
        if (task.getDueDate() == null ||
                task.getDueDate().isEmpty() ||
                task.getDueTime() == null ||
                task.getDueTime().isEmpty()) {

            return;
        }

        try {

            // Combine date and time.
            String dateTime =
                    task.getDueDate() + " " + task.getDueTime();

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            Locale.US
                    );

            format.setLenient(false);

            Date taskDate = format.parse(dateTime);

            if (taskDate == null) {
                return;
            }

            Calendar reminderCalendar = Calendar.getInstance();

            reminderCalendar.setTime(taskDate);

            // Calculate reminder offset.
            long offset = getReminderOffset(task.getReminder());

            if (offset < 0) {
                return;
            }

            reminderCalendar.add(
                    Calendar.MINUTE,
                    (int) -offset
            );

            long reminderTime =
                    reminderCalendar.getTimeInMillis();

            // Do not schedule reminders in the past.
            if (reminderTime <= System.currentTimeMillis()) {
                return;
            }

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager == null) {
                return;
            }

            Intent intent =
                    new Intent(context, ReminderReceiver.class);

            intent.setAction(ACTION_TASK_REMINDER);

            intent.setData(
                    Uri.parse("taskmate://reminder/" + task.getId())
            );

            intent.putExtra("taskTitle", task.getTitle());

            intent.putExtra(
                    "taskDescription",
                    task.getDescription()
            );

            intent.putExtra(
                    "notificationId",
                    task.getId()
            );

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            task.getId(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            // Use exact alarm when permission is available.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (alarmManager.canScheduleExactAlarms()) {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );

                } else {

                    // Fallback to an inexact alarm.
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                }

            } else {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // Convert reminder option into minutes.
    private static long getReminderOffset(String reminder) {

        switch (reminder) {

            case "At time of task":
                return 0;

            case "5 minutes before":
                return 5;

            case "10 minutes before":
                return 10;

            case "30 minutes before":
                return 30;

            case "1 hour before":
                return 60;

            case "1 day before":
                return 24 * 60;

            default:
                return -1;
        }
    }

    // Cancel a previously scheduled reminder.
    public static void cancelReminder(
            Context context,
            int taskId
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        Intent intent =
                new Intent(context, ReminderReceiver.class);

        intent.setAction(ACTION_TASK_REMINDER);

        intent.setData(
                Uri.parse("taskmate://reminder/" + taskId)
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        taskId,
                        intent,
                        PendingIntent.FLAG_NO_CREATE |
                                PendingIntent.FLAG_IMMUTABLE
                );

        if (alarmManager != null && pendingIntent != null) {

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
