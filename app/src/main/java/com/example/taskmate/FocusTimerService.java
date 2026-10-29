
package com.example.taskmate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class FocusTimerService extends Service {

    public static final String ACTION_START =
            "com.example.taskmate.START_TIMER";

    public static final String ACTION_STOP =
            "com.example.taskmate.STOP_TIMER";

    public static final String ACTION_TICK =
            "com.example.taskmate.TIMER_TICK";

    public static final String ACTION_FINISHED =
            "com.example.taskmate.TIMER_FINISHED";

    public static final String EXTRA_DURATION =
            "durationMillis";

    public static final String EXTRA_REMAINING =
            "remainingMillis";

    private static final String CHANNEL_ID = "focus_timer_channel";
    private static final int NOTIFICATION_ID = 101;

    private CountDownTimer countDownTimer;
    private long remainingMillis;
    private boolean timerRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_START.equals(action)) {

            long duration = intent.getLongExtra(
                    EXTRA_DURATION,
                    25 * 60 * 1000L
            );

            startTimer(duration);

        } else if (ACTION_STOP.equals(action)) {

            stopTimer();
            stopSelf();

        }

        return START_NOT_STICKY;
    }

    private void startTimer(long duration) {

        stopTimer();

        remainingMillis = Math.max(1000L, duration);

        // Start the foreground service with a notification.
        startForeground(
                NOTIFICATION_ID,
                buildNotification(remainingMillis)
        );

        timerRunning = true;

        countDownTimer = new CountDownTimer(remainingMillis, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;

                updateNotification(remainingMillis);

                Intent updateIntent = new Intent(ACTION_TICK);
                updateIntent.setPackage(getPackageName());
                updateIntent.putExtra(
                        EXTRA_REMAINING,
                        remainingMillis
                );

                sendBroadcast(updateIntent);
            }

            @Override
            public void onFinish() {

                remainingMillis = 0;
                timerRunning = false;
                countDownTimer = null;

                updateNotification(0);

                Intent finishedIntent = new Intent(ACTION_FINISHED);
                finishedIntent.setPackage(getPackageName());
                sendBroadcast(finishedIntent);

                stopForeground(STOP_FOREGROUND_DETACH);
                stopSelf();
            }
        };

        countDownTimer.start();
    }

    private void stopTimer() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        timerRunning = false;
        remainingMillis = 0;

        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    private Notification buildNotification(long millis) {

        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        String timeText = String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );

        Intent openAppIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TaskMate Focus Timer")
                .setContentText("Time remaining: " + timeText)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentIntent(pendingIntent)
                .setOngoing(timerRunning)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void updateNotification(long millis) {

        Notification notification = buildNotification(millis);

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Focus Timer",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription(
                    "Shows the remaining time for a focus session."
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        timerRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
