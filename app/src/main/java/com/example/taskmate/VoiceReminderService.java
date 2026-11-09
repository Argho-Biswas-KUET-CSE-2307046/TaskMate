
package com.example.taskmate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class VoiceReminderService extends Service {

    public static final String ACTION_STOP =
            "com.example.taskmate.STOP_VOICE_REMINDER";

    public static final String EXTRA_DESCRIPTION = "description";

    private static final String TAG = "VoiceReminderService";

    private static final String CHANNEL_ID =
            "voice_reminder_channel";

    private static final int NOTIFICATION_ID = 9001;

    private TextToSpeech textToSpeech;

    private String description = "";

    private boolean ttsReady = false;
    private boolean stopRequested = false;
    private boolean speechStarted = false;

    private int repeatCount = 0;
    private int maximumRepeats = 5;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        textToSpeech = new TextToSpeech(
                getApplicationContext(),
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        int result = textToSpeech.setLanguage(
                                Locale.getDefault()
                        );

                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {

                            Log.e(TAG, "Language not supported");
                            stopVoiceService();
                            return;
                        }

                        ttsReady = true;
                        startSpeakingIfReady();

                    } else {

                        Log.e(TAG, "TTS initialization failed");
                        stopVoiceService();
                    }
                }
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            stopVoiceService();
            return START_NOT_STICKY;
        }

        // Handle Stop Voice action
        if (ACTION_STOP.equals(intent.getAction())) {

            stopVoiceService();
            return START_NOT_STICKY;
        }

        String newDescription =
                intent.getStringExtra(EXTRA_DESCRIPTION);

        if (newDescription == null ||
                newDescription.trim().isEmpty()) {

            stopVoiceService();
            return START_NOT_STICKY;
        }

        description = newDescription.trim();

        repeatCount = 0;
        stopRequested = false;
        speechStarted = false;

        // Check current battery percentage
        int batteryLevel = getBatteryLevel();

        if (batteryLevel > 20) {
            maximumRepeats = Integer.MAX_VALUE;
        } else {
            maximumRepeats = 5;
        }

        // Start foreground notification immediately
        startForeground(
                NOTIFICATION_ID,
                createVoiceNotification()
        );

        startSpeakingIfReady();

        return START_NOT_STICKY;
    }

    private void startSpeakingIfReady() {

        if (!ttsReady ||
                stopRequested ||
                speechStarted ||
                textToSpeech == null ||
                description.isEmpty()) {

            return;
        }

        speechStarted = true;

        textToSpeech.setOnUtteranceProgressListener(
                new UtteranceProgressListener() {

                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "Speaking reminder");
                    }

                    @Override
                    public void onDone(String utteranceId) {

                        if (stopRequested) {
                            return;
                        }

                        repeatCount++;

                        if (repeatCount < maximumRepeats) {

                            speakAgain();

                        } else {

                            stopVoiceService();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {

                        Log.e(TAG, "Speech error");

                        stopVoiceService();
                    }
                }
        );

        speakAgain();
    }

    private void speakAgain() {

        if (stopRequested || textToSpeech == null) {
            return;
        }

        String utteranceId = "TASK_REMINDER_" + repeatCount;

        int result = textToSpeech.speak(
                description,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
        );

        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Unable to start speech");
            stopVoiceService();
        }
    }

    private int getBatteryLevel() {

        BatteryManager batteryManager =
                (BatteryManager) getSystemService(BATTERY_SERVICE);

        if (batteryManager == null) {
            return 100;
        }

        int level = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
        );

        return level < 0 ? 100 : level;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Voice Task Reminders",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Keeps task voice reminders running"
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createVoiceNotification() {

        // Intent for Stop Voice button
        Intent stopIntent = new Intent(
                this,
                VoiceReminderService.class
        );

        stopIntent.setAction(ACTION_STOP);

        PendingIntent stopPendingIntent =
                PendingIntent.getService(
                        this,
                        1001,
                        stopIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        return new NotificationCompat.Builder(this, CHANNEL_ID)

                .setSmallIcon(android.R.drawable.ic_btn_speak_now)

                .setContentTitle("TaskMate Voice Reminder")

                .setContentText("Reading your task description...")

                .setPriority(NotificationCompat.PRIORITY_LOW)

                .setOngoing(true)

                .addAction(
                        android.R.drawable.ic_media_pause,
                        "Stop Voice",
                        stopPendingIntent
                )

                .build();
    }

    private void stopVoiceService() {

        stopRequested = true;

        if (textToSpeech != null) {
            textToSpeech.stop();
        }

        stopForeground(STOP_FOREGROUND_REMOVE);

        stopSelf();
    }

    @Override
    public void onDestroy() {

        stopRequested = true;

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();

            textToSpeech = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
