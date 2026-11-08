
package com.example.taskmate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class FocusTimerService extends Service {

    // -----------------------------------------
    // ACTIONS
    // -----------------------------------------

    public static final String ACTION_START =
            "com.example.taskmate.START_TIMER";

    public static final String ACTION_PAUSE =
            "com.example.taskmate.PAUSE_TIMER";

    public static final String ACTION_RESUME =
            "com.example.taskmate.RESUME_TIMER";

    public static final String ACTION_STOP =
            "com.example.taskmate.STOP_TIMER";

    public static final String ACTION_STOP_ALARM =
            "com.example.taskmate.STOP_ALARM";

    public static final String ACTION_ALARM_TRIGGER =
            "com.example.taskmate.ALARM_TRIGGER";

    public static final String ACTION_GET_STATE =
            "com.example.taskmate.GET_TIMER_STATE";

    public static final String ACTION_STATE =
            "com.example.taskmate.TIMER_STATE";

    public static final String ACTION_TICK =
            "com.example.taskmate.TIMER_TICK";

    public static final String ACTION_FINISHED =
            "com.example.taskmate.TIMER_FINISHED";


    // -----------------------------------------
    // EXTRAS
    // -----------------------------------------

    public static final String EXTRA_DURATION = "durationMillis";

    public static final String EXTRA_REMAINING = "remainingMillis";

    public static final String EXTRA_RUNNING = "timerRunning";

    public static final String EXTRA_ALARM_ACTIVE = "alarmActive";


    // -----------------------------------------
    // NOTIFICATION SETTINGS
    // -----------------------------------------

    private static final String CHANNEL_ID =
            "focus_timer_channel";

    private static final int NOTIFICATION_ID = 101;


    // -----------------------------------------
    // ALARM SETTINGS
    // -----------------------------------------

    // Alarm sounds for 1 minute.
    private static final long ALARM_DURATION = 60 * 1000L;

    // One complete alarm cycle is 5 minutes.
    private static final long ALARM_INTERVAL = 5 * 60 * 1000L;

    // WakeLock timeout: 10 minutes.
    private static final long WAKELOCK_TIMEOUT = 10 * 60 * 1000L;


    // -----------------------------------------
    // TIMER VARIABLES
    // -----------------------------------------

    private CountDownTimer countDownTimer;

    private long remainingMillis = 0;

    private long totalDurationMillis = 25 * 60 * 1000L;

    private boolean timerRunning = false;

    private boolean alarmCycleActive = false;

    private boolean foregroundStarted = false;


    // -----------------------------------------
    // ALARM SOUND VARIABLES
    // -----------------------------------------

    private MediaPlayer mediaPlayer;

    private final Handler alarmHandler =
            new Handler(Looper.getMainLooper());

    private Runnable stopSoundRunnable;

    private Runnable nextAlarmRunnable;


    // -----------------------------------------
    // WAKELOCK
    // -----------------------------------------

    private PowerManager.WakeLock alarmWakeLock;


    // -----------------------------------------
    // SERVICE CREATION
    // -----------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        PowerManager powerManager =
                (PowerManager) getSystemService(POWER_SERVICE);

        if (powerManager != null) {
            alarmWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    getPackageName() + ":FocusAlarm"
            );

            alarmWakeLock.setReferenceCounted(false);
        }
    }


    // -----------------------------------------
    // HANDLE SERVICE ACTIONS
    // -----------------------------------------

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        if (intent == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();


        // START TIMER
        if (ACTION_START.equals(action)) {

            stopAlarmCycle();

            stopCountdown();

            AlarmScheduler.cancelAlarm(this);

            long duration = intent.getLongExtra(
                    EXTRA_DURATION,
                    25 * 60 * 1000L
            );

            totalDurationMillis = Math.max(1000L, duration);

            remainingMillis = totalDurationMillis;

            AlarmScheduler.scheduleAlarm(
                    this,
                    remainingMillis
            );

            beginCountdown();
        }


        // PAUSE TIMER
        else if (ACTION_PAUSE.equals(action)) {

            pauseTimer();
        }


        // RESUME TIMER
        else if (ACTION_RESUME.equals(action)) {

            resumeTimer();
        }


        // STOP TIMER
        else if (ACTION_STOP.equals(action)) {

            AlarmScheduler.cancelAlarm(this);

            stopAlarmCycle();

            stopCountdown();

            remainingMillis = 0;

            totalDurationMillis = 0;

            removeForegroundNotification();

            stopSelf();
        }


        // STOP REPEATING ALARM
        else if (ACTION_STOP_ALARM.equals(action)) {

            AlarmScheduler.cancelAlarm(this);

            stopAlarmCycle();

            removeForegroundNotification();

            stopSelf();
        }


        // ALARM TRIGGERED BY ALARM RECEIVER
        else if (ACTION_ALARM_TRIGGER.equals(action)) {

            if (!alarmCycleActive) {

                // The scheduled deadline has been reached.
                AlarmScheduler.cancelAlarm(this);

                stopCountdown();

                remainingMillis = 0;

                sendTimerBroadcast(
                        ACTION_FINISHED,
                        0
                );

                startAlarmCycle();
            }
        }


        // GET CURRENT TIMER STATE
        else if (ACTION_GET_STATE.equals(action)) {

            Intent stateIntent = new Intent(ACTION_STATE);

            stateIntent.setPackage(getPackageName());

            stateIntent.putExtra(
                    EXTRA_REMAINING,
                    remainingMillis
            );

            stateIntent.putExtra(
                    EXTRA_DURATION,
                    totalDurationMillis
            );

            stateIntent.putExtra(
                    EXTRA_RUNNING,
                    timerRunning
            );

            stateIntent.putExtra(
                    EXTRA_ALARM_ACTIVE,
                    alarmCycleActive
            );

            sendBroadcast(stateIntent);

            if (!timerRunning &&
                    !alarmCycleActive &&
                    remainingMillis <= 0) {

                stopSelf(startId);
            }
        }

        return START_NOT_STICKY;
    }


    // -----------------------------------------
    // START COUNTDOWN
    // -----------------------------------------

    private void beginCountdown() {

        if (remainingMillis <= 0) {
            return;
        }

        timerRunning = true;

        startForeground(
                NOTIFICATION_ID,
                buildNotification(remainingMillis)
        );

        foregroundStarted = true;

        countDownTimer = new CountDownTimer(
                remainingMillis,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                remainingMillis = millisUntilFinished;

                updateNotification(remainingMillis);

                sendTimerBroadcast(
                        ACTION_TICK,
                        remainingMillis
                );
            }


            @Override
            public void onFinish() {

                remainingMillis = 0;

                timerRunning = false;

                countDownTimer = null;

                AlarmScheduler.cancelAlarm(
                        FocusTimerService.this
                );

                sendTimerBroadcast(
                        ACTION_FINISHED,
                        0
                );

                startAlarmCycle();
            }

        };

        countDownTimer.start();
    }


    // -----------------------------------------
    // PAUSE TIMER
    // -----------------------------------------

    private void pauseTimer() {

        if (countDownTimer != null) {

            countDownTimer.cancel();

            countDownTimer = null;
        }

        timerRunning = false;

        AlarmScheduler.cancelAlarm(this);

        updateNotification(remainingMillis);

        sendTimerBroadcast(
                ACTION_TICK,
                remainingMillis
        );
    }


    // -----------------------------------------
    // RESUME TIMER
    // -----------------------------------------

    private void resumeTimer() {

        if (!timerRunning && remainingMillis > 0) {

            AlarmScheduler.scheduleAlarm(
                    this,
                    remainingMillis
            );

            beginCountdown();
        }
    }


    // -----------------------------------------
    // STOP COUNTDOWN
    // -----------------------------------------

    private void stopCountdown() {

        if (countDownTimer != null) {

            countDownTimer.cancel();

            countDownTimer = null;
        }

        timerRunning = false;
    }


    // -----------------------------------------
    // ACQUIRE WAKELOCK
    // -----------------------------------------

    private void acquireAlarmWakeLock() {

        if (alarmWakeLock == null) {
            return;
        }

        try {

            // Refresh the timeout for every new alarm cycle.
            if (alarmWakeLock.isHeld()) {
                alarmWakeLock.release();
            }

            alarmWakeLock.acquire(WAKELOCK_TIMEOUT);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // -----------------------------------------
    // RELEASE WAKELOCK
    // -----------------------------------------

    private void releaseAlarmWakeLock() {

        if (alarmWakeLock != null &&
                alarmWakeLock.isHeld()) {

            try {

                alarmWakeLock.release();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    // -----------------------------------------
    // START ALARM CYCLE
    // -----------------------------------------

    private void startAlarmCycle() {

        if (alarmCycleActive) {
            return;
        }

        alarmCycleActive = true;

        if (!foregroundStarted) {

            startForeground(
                    NOTIFICATION_ID,
                    buildNotification(0)
            );

            foregroundStarted = true;
        }

        updateNotification(0);

        playAlarmSound();
    }


    // -----------------------------------------
    // PLAY ALARM SOUND
    // -----------------------------------------

    private void playAlarmSound() {

        if (!alarmCycleActive) {
            return;
        }

        // Keep the CPU awake while the alarm cycle is active.
        acquireAlarmWakeLock();

        stopAlarmSound();

        try {

            Uri alarmUri = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
            );

            if (alarmUri == null) {

                alarmUri = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                );
            }

            if (alarmUri == null) {

                throw new IllegalStateException(
                        "No alarm sound is available."
                );
            }

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(
                    getApplicationContext(),
                    alarmUri
            );

            AudioAttributes attributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                            )
                            .build();

            mediaPlayer.setAudioAttributes(attributes);

            mediaPlayer.setLooping(true);

            mediaPlayer.prepare();

            mediaPlayer.start();

        } catch (Exception e) {

            e.printStackTrace();

            stopAlarmSound();
        }


        // Stop sound after one minute.
        stopSoundRunnable = () -> {

            stopAlarmSound();

            // Wait four minutes before the next alarm.
            scheduleNextAlarm();
        };

        alarmHandler.postDelayed(
                stopSoundRunnable,
                ALARM_DURATION
        );

        updateNotification(0);
    }


    // -----------------------------------------
    // SCHEDULE NEXT ALARM
    // -----------------------------------------

    private void scheduleNextAlarm() {

        if (!alarmCycleActive) {
            return;
        }

        nextAlarmRunnable = () -> {

            if (alarmCycleActive) {

                playAlarmSound();
            }
        };

        alarmHandler.postDelayed(
                nextAlarmRunnable,
                ALARM_INTERVAL - ALARM_DURATION
        );
    }


    // -----------------------------------------
    // STOP ALARM SOUND
    // -----------------------------------------

    private void stopAlarmSound() {

        if (mediaPlayer != null) {

            try {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                mediaPlayer.release();

            } catch (Exception e) {

                e.printStackTrace();
            }

            mediaPlayer = null;
        }
    }


    // -----------------------------------------
    // STOP ALARM CYCLE
    // -----------------------------------------

    private void stopAlarmCycle() {

        alarmCycleActive = false;

        if (stopSoundRunnable != null) {

            alarmHandler.removeCallbacks(stopSoundRunnable);

            stopSoundRunnable = null;
        }

        if (nextAlarmRunnable != null) {

            alarmHandler.removeCallbacks(nextAlarmRunnable);

            nextAlarmRunnable = null;
        }

        stopAlarmSound();

        // Release CPU lock when the user stops the alarm.
        releaseAlarmWakeLock();
    }


    // -----------------------------------------
    // REMOVE FOREGROUND NOTIFICATION
    // -----------------------------------------

    private void removeForegroundNotification() {

        if (foregroundStarted) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                stopForeground(STOP_FOREGROUND_REMOVE);

            } else {

                stopForeground(true);
            }

            foregroundStarted = false;
        }
    }


    // -----------------------------------------
    // BUILD NOTIFICATION
    // -----------------------------------------

    private Notification buildNotification(long millis) {

        String title;

        String message;

        if (alarmCycleActive) {

            title = "Focus Session Completed!";

            message = "Your alarm is repeating until you stop it.";

        } else {

            long totalSeconds = millis / 1000;

            long minutes = totalSeconds / 60;

            long seconds = totalSeconds % 60;

            String timeText = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    minutes,
                    seconds
            );

            title = "TaskMate Focus Timer";

            message = "Time remaining: " + timeText;
        }


        Intent openAppIntent = new Intent(
                this,
                FocusTimerActivity.class
        );

        PendingIntent openPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(
                                android.R.drawable.ic_lock_idle_alarm
                        )
                        .setContentIntent(openPendingIntent)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


        if (alarmCycleActive) {

            Intent stopIntent = new Intent(
                    this,
                    FocusTimerService.class
            );

            stopIntent.setAction(ACTION_STOP_ALARM);

            PendingIntent stopPendingIntent =
                    PendingIntent.getService(
                            this,
                            102,
                            stopIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            builder.addAction(
                    android.R.drawable.ic_media_pause,
                    "STOP ALARM",
                    stopPendingIntent
            );
        }

        return builder.build();
    }


    // -----------------------------------------
    // UPDATE NOTIFICATION
    // -----------------------------------------

    private void updateNotification(long millis) {

        Notification notification = buildNotification(millis);

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        if (manager != null) {

            manager.notify(
                    NOTIFICATION_ID,
                    notification
            );
        }
    }


    // -----------------------------------------
    // CREATE NOTIFICATION CHANNEL
    // -----------------------------------------

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Focus Timer",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Focus timer and repeating alarm notifications."
            );

            channel.setSound(null, null);

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {

                manager.createNotificationChannel(channel);
            }
        }
    }


    // -----------------------------------------
    // SEND TIMER BROADCAST
    // -----------------------------------------

    private void sendTimerBroadcast(
            String action,
            long millis
    ) {

        Intent intent = new Intent(action);

        intent.setPackage(getPackageName());

        intent.putExtra(
                EXTRA_REMAINING,
                millis
        );

        sendBroadcast(intent);
    }


    // -----------------------------------------
    // SERVICE DESTROYED
    // -----------------------------------------

    @Override
    public void onDestroy() {

        stopCountdown();

        stopAlarmCycle();

        removeForegroundNotification();

        super.onDestroy();
    }


    // -----------------------------------------
    // BIND SERVICE
    // -----------------------------------------

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
