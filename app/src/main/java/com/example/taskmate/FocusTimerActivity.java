
package com.example.taskmate;

import android.Manifest;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class FocusTimerActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 201;

    private TextView tvTimer;
    private TextView tvTimerStatus;

    private EditText etCustomMinutes;

    private Button btn25Min;
    private Button btn50Min;
    private Button btnCustom;
    private Button btnStart;
    private Button btnPause;
    private Button btnReset;

    private CircularProgressView circularProgress;

    private long selectedDuration = 25 * 60 * 1000L;
    private long remainingMillis = selectedDuration;

    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean receiverRegistered = false;


    // -----------------------------------------
    // RECEIVE BROADCASTS FROM SERVICE
    // -----------------------------------------

    private final BroadcastReceiver timerReceiver =
            new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();

                    // TIMER TICK
                    if (FocusTimerService.ACTION_TICK.equals(action)) {

                        remainingMillis = intent.getLongExtra(
                                FocusTimerService.EXTRA_REMAINING,
                                0
                        );

                        updateTimerDisplay();
                        updateCircularProgress();
                    }

                    // TIMER FINISHED
                    else if (FocusTimerService.ACTION_FINISHED.equals(action)) {

                        remainingMillis = 0;

                        isRunning = false;
                        isPaused = false;

                        updateTimerDisplay();
                        updateCircularProgress();
                        updateButtonText();

                        tvTimerStatus.setText("Focus session completed!");

                        Toast.makeText(
                                FocusTimerActivity.this,
                                "Focus session completed! Check your alarm notification.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    // RESTORE TIMER STATE
                    else if (FocusTimerService.ACTION_STATE.equals(action)) {

                        restoreTimerState(intent);
                    }
                }
            };


    // -----------------------------------------
    // ACTIVITY CREATION
    // -----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_focus_timer);

        initializeViews();

        setClickListeners();

        updateTimerDisplay();
        updateCircularProgress();
        updateButtonText();

        requestNotificationPermission();
    }


    // -----------------------------------------
    // INITIALIZE VIEWS
    // -----------------------------------------

    private void initializeViews() {

        tvTimer = findViewById(R.id.tvTimer);

        tvTimerStatus = findViewById(R.id.tvTimerStatus);

        etCustomMinutes = findViewById(R.id.etCustomMinutes);

        btn25Min = findViewById(R.id.btn25Min);

        btn50Min = findViewById(R.id.btn50Min);

        btnCustom = findViewById(R.id.btnCustom);

        btnStart = findViewById(R.id.btnStart);

        btnPause = findViewById(R.id.btnPause);

        btnReset = findViewById(R.id.btnReset);

        circularProgress = findViewById(R.id.circularProgress);

        TextView btnBack = findViewById(R.id.btnFocusBack);

        btnBack.setOnClickListener(v -> finish());
    }


    // -----------------------------------------
    // BUTTON CLICK LISTENERS
    // -----------------------------------------

    private void setClickListeners() {

        btn25Min.setOnClickListener(v -> selectDuration(25));

        btn50Min.setOnClickListener(v -> selectDuration(50));

        // CUSTOM DURATION
        btnCustom.setOnClickListener(v -> {

            String input = etCustomMinutes.getText()
                    .toString()
                    .trim();

            if (input.isEmpty()) {

                etCustomMinutes.setError("Enter duration");

                return;
            }

            try {

                int minutes = Integer.parseInt(input);

                if (minutes < 1 || minutes > 1440) {

                    etCustomMinutes.setError(
                            "Enter between 1 and 1440 minutes"
                    );

                    return;
                }

                selectDuration(minutes);

            } catch (NumberFormatException e) {

                etCustomMinutes.setError("Invalid duration");
            }
        });


        // START OR RESUME
        btnStart.setOnClickListener(v -> {

            if (isRunning) {
                return;
            }

            if (!checkTimerPermissions()) {
                return;
            }

            if (isPaused && remainingMillis > 0) {

                resumeTimer();

            } else {

                startTimer();
            }
        });


        // PAUSE
        btnPause.setOnClickListener(v -> pauseTimer());


        // RESET
        btnReset.setOnClickListener(v -> resetTimer());
    }


    // -----------------------------------------
    // NOTIFICATION PERMISSION
    // -----------------------------------------

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_REQUEST
                );
            }
        }
    }


    // -----------------------------------------
    // CHECK TIMER PERMISSIONS
    // -----------------------------------------

    private boolean checkTimerPermissions() {

        // Check notification permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestNotificationPermission();

                Toast.makeText(
                        this,
                        "Please allow notifications before starting the timer.",
                        Toast.LENGTH_LONG
                ).show();

                return false;
            }
        }


        // Check exact alarm permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager != null &&
                    !alarmManager.canScheduleExactAlarms()) {

                Toast.makeText(
                        this,
                        "Please allow Alarms and reminders for TaskMate.",
                        Toast.LENGTH_LONG
                ).show();

                Intent intent = new Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName())
                );

                try {

                    startActivity(intent);

                } catch (Exception e) {

                    e.printStackTrace();

                    Toast.makeText(
                            this,
                            "Open TaskMate app settings and allow Alarms and reminders.",
                            Toast.LENGTH_LONG
                    ).show();
                }

                return false;
            }
        }

        return true;
    }


    // -----------------------------------------
    // SELECT TIMER DURATION
    // -----------------------------------------

    private void selectDuration(int minutes) {

        if (isRunning || isPaused) {

            stopServiceTimer();
        }

        selectedDuration = minutes * 60 * 1000L;

        remainingMillis = selectedDuration;

        isRunning = false;
        isPaused = false;

        updateTimerDisplay();
        updateCircularProgress();
        updateButtonText();

        tvTimerStatus.setText("Ready to focus");

        etCustomMinutes.clearFocus();
        etCustomMinutes.setError(null);
    }


    // -----------------------------------------
    // START TIMER
    // -----------------------------------------

    private void startTimer() {

        remainingMillis = selectedDuration;

        Intent intent = new Intent(
                this,
                FocusTimerService.class
        );

        intent.setAction(FocusTimerService.ACTION_START);

        intent.putExtra(
                FocusTimerService.EXTRA_DURATION,
                remainingMillis
        );

        ContextCompat.startForegroundService(this, intent);

        isRunning = true;
        isPaused = false;

        updateButtonText();

        tvTimerStatus.setText("Stay focused!");
    }


    // -----------------------------------------
    // RESUME TIMER
    // -----------------------------------------

    private void resumeTimer() {

        if (!checkTimerPermissions()) {
            return;
        }

        Intent intent = new Intent(
                this,
                FocusTimerService.class
        );

        intent.setAction(FocusTimerService.ACTION_RESUME);

        ContextCompat.startForegroundService(this, intent);

        isRunning = true;
        isPaused = false;

        updateButtonText();

        tvTimerStatus.setText("Stay focused!");
    }


    // -----------------------------------------
    // PAUSE TIMER
    // -----------------------------------------

    private void pauseTimer() {

        if (!isRunning) {
            return;
        }

        Intent intent = new Intent(
                this,
                FocusTimerService.class
        );

        intent.setAction(FocusTimerService.ACTION_PAUSE);

        startService(intent);

        isRunning = false;
        isPaused = true;

        updateButtonText();

        tvTimerStatus.setText("Timer paused");
    }


    // -----------------------------------------
    // RESET TIMER
    // -----------------------------------------

    private void resetTimer() {

        stopServiceTimer();

        remainingMillis = selectedDuration;

        isRunning = false;
        isPaused = false;

        updateTimerDisplay();
        updateCircularProgress();
        updateButtonText();

        tvTimerStatus.setText("Ready to focus");
    }


    // -----------------------------------------
    // STOP SERVICE TIMER
    // -----------------------------------------

    private void stopServiceTimer() {

        Intent intent = new Intent(
                this,
                FocusTimerService.class
        );

        intent.setAction(FocusTimerService.ACTION_STOP);

        startService(intent);
    }


    // -----------------------------------------
    // UPDATE DIGITAL TIMER
    // -----------------------------------------

    private void updateTimerDisplay() {

        long totalSeconds = remainingMillis / 1000;

        long minutes = totalSeconds / 60;

        long seconds = totalSeconds % 60;

        String time = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );

        tvTimer.setText(time);
    }


    // -----------------------------------------
    // UPDATE CIRCULAR PROGRESS
    // -----------------------------------------

    private void updateCircularProgress() {

        if (selectedDuration <= 0) {

            circularProgress.setProgress(0);

            return;
        }

        float progress = (float) remainingMillis /
                (float) selectedDuration;

        circularProgress.setProgress(progress);
    }


    // -----------------------------------------
    // UPDATE START BUTTON TEXT
    // -----------------------------------------

    private void updateButtonText() {

        if (isPaused) {

            btnStart.setText("Resume");

        } else {

            btnStart.setText("Start");
        }
    }


    // -----------------------------------------
    // RESTORE TIMER STATE
    // -----------------------------------------

    private void restoreTimerState(Intent intent) {

        long recoveredRemaining = intent.getLongExtra(
                FocusTimerService.EXTRA_REMAINING,
                0
        );

        long recoveredDuration = intent.getLongExtra(
                FocusTimerService.EXTRA_DURATION,
                selectedDuration
        );

        boolean recoveredRunning = intent.getBooleanExtra(
                FocusTimerService.EXTRA_RUNNING,
                false
        );

        boolean recoveredAlarm = intent.getBooleanExtra(
                FocusTimerService.EXTRA_ALARM_ACTIVE,
                false
        );


        if (!recoveredRunning &&
                !recoveredAlarm &&
                recoveredRemaining <= 0) {

            return;
        }


        if (recoveredDuration > 0) {

            selectedDuration = recoveredDuration;
        }

        remainingMillis = recoveredRemaining;

        isRunning = recoveredRunning;

        isPaused = !recoveredRunning &&
                !recoveredAlarm &&
                recoveredRemaining > 0;


        updateTimerDisplay();

        updateCircularProgress();

        updateButtonText();


        if (recoveredAlarm) {

            tvTimerStatus.setText(
                    "Focus session completed! Alarm active"
            );

            isRunning = false;
            isPaused = false;

            updateButtonText();
        }

        else if (recoveredRunning) {

            tvTimerStatus.setText("Stay focused!");
        }

        else if (isPaused) {

            tvTimerStatus.setText("Timer paused");
        }
    }


    // -----------------------------------------
    // REGISTER TIMER RECEIVER
    // -----------------------------------------

    private void registerTimerReceiver() {

        if (receiverRegistered) {
            return;
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(FocusTimerService.ACTION_TICK);

        filter.addAction(FocusTimerService.ACTION_FINISHED);

        filter.addAction(FocusTimerService.ACTION_STATE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            registerReceiver(
                    timerReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED
            );

        } else {

            registerReceiver(timerReceiver, filter);
        }

        receiverRegistered = true;
    }


    // -----------------------------------------
    // REQUEST CURRENT TIMER STATE
    // -----------------------------------------

    private void requestTimerState() {

        Intent intent = new Intent(
                this,
                FocusTimerService.class
        );

        intent.setAction(FocusTimerService.ACTION_GET_STATE);

        startService(intent);
    }


    // -----------------------------------------
    // ACTIVITY STARTED
    // -----------------------------------------

    @Override
    protected void onStart() {

        super.onStart();

        registerTimerReceiver();

        requestTimerState();
    }


    // -----------------------------------------
    // ACTIVITY STOPPED
    // -----------------------------------------

    @Override
    protected void onStop() {

        super.onStop();

        if (receiverRegistered) {

            unregisterReceiver(timerReceiver);

            receiverRegistered = false;
        }
    }
}
