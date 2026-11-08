
package com.example.taskmate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmScheduler {

    public static final String ACTION_TIMER_ALARM =
            "com.example.taskmate.TIMER_ALARM";

    private static final String PREFS_NAME =
            "TaskMateTimerPrefs";

    private static final String KEY_DEADLINE =
            "timerDeadline";

    private static final int ALARM_REQUEST_CODE = 501;


    // Schedule the timer using a real-world deadline.
    public static void scheduleAlarm(
            Context context,
            long durationMillis
    ) {

        long deadline =
                System.currentTimeMillis() + durationMillis;

        // Save the deadline so it survives app closure and reboot.
        context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                ).edit()
                .putLong(KEY_DEADLINE, deadline)
                .apply();

        scheduleDeadline(context, deadline);
    }


    // Schedule the saved deadline with Android.
    public static void scheduleDeadline(
            Context context,
            long deadline
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(
                context,
                AlarmReceiver.class
        );

        intent.setAction(ACTION_TIMER_ALARM);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        ALARM_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        if (deadline <= System.currentTimeMillis()) {

            // The deadline has already passed.
            // Trigger the receiver immediately.
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(),
                    pendingIntent
            );

            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        deadline,
                        pendingIntent
                );

            } else {

                // Temporary fallback until exact-alarm
                // permission is granted.
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        deadline,
                        pendingIntent
                );
            }

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    deadline,
                    pendingIntent
            );
        }
    }


    // Get the saved deadline.
    public static long getSavedDeadline(Context context) {

        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        ).getLong(KEY_DEADLINE, 0);
    }


    // Cancel the scheduled timer.
    public static void cancelAlarm(Context context) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        Intent intent = new Intent(
                context,
                AlarmReceiver.class
        );

        intent.setAction(ACTION_TIMER_ALARM);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        ALARM_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        if (alarmManager != null) {

            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();

        context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                ).edit()
                .remove(KEY_DEADLINE)
                .apply();
    }
}
