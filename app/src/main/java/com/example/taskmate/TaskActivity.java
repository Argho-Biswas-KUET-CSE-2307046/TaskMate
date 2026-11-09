package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvTodayDate;

    private TextView tvTotalTasks;
    private TextView tvCompletedTasks;
    private TextView tvPendingTasks;
    private TextView tvOverdueTasks;

    private TextView tvViewAll;

    private LinearLayout actionAddTask;
    private LinearLayout actionProgress;
    private LinearLayout actionFocus;

    private LinearLayout navTasks;
    private LinearLayout navProgress;
    private LinearLayout navProfile;

    private TaskDatabase database;
    private FirebaseTaskRepository firebaseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        database = new TaskDatabase(this);

        initializeViews();
        showUserInformation();
        showCurrentDate();
        setupNavigation();
        updateStatistics();

        initializeFirebase();
        addFirebaseTestControls();

        // Add navigation buttons for the new features.
        addFeatureNavigationControls();
    }

    // Connect Java variables with XML views.
    private void initializeViews() {

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTodayDate = findViewById(R.id.tvTodayDate);

        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        tvOverdueTasks = findViewById(R.id.tvOverdueTasks);

        tvViewAll = findViewById(R.id.tvViewAll);

        actionAddTask = findViewById(R.id.actionAddTask);
        actionProgress = findViewById(R.id.actionProgress);
        actionFocus = findViewById(R.id.actionFocus);

        navTasks = findViewById(R.id.navTasks);
        navProgress = findViewById(R.id.navProgress);
        navProfile = findViewById(R.id.navProfile);
    }

    // Initialize Firebase repository.
    private void initializeFirebase() {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please log in first.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        try {

            firebaseRepository = new FirebaseTaskRepository();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Firebase initialization failed: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Display the logged-in user's name.
    private void showUserInformation() {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        String name = "there";

        if (user != null) {

            if (user.getDisplayName() != null &&
                    !user.getDisplayName().trim().isEmpty()) {

                name = user.getDisplayName();

            } else if (user.getEmail() != null) {

                name = user.getEmail().split("@")[0];
            }
        }

        tvWelcome.setText("Hello, " + name + "! 👋");
    }

    // Display today's date.
    private void showCurrentDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEEE, MMMM d, yyyy",
                Locale.getDefault()
        );

        tvTodayDate.setText(dateFormat.format(new Date()));
    }

    // Open the My Tasks screen.
    private void openTasks() {

        Intent intent = new Intent(
                TaskActivity.this,
                TasksActivity.class
        );

        startActivity(intent);
    }

    // Open the Profile screen.
    private void openProfile() {

        Intent intent = new Intent(
                TaskActivity.this,
                ProfileActivity.class
        );

        startActivity(intent);
    }

    // Open the Focus Timer screen.
    private void openFocusTimer() {

        Intent intent = new Intent(
                TaskActivity.this,
                FocusTimerActivity.class
        );

        startActivity(intent);
    }

    // Open the JSON/XML Parsing screen.
    private void openDataParsing() {

        Intent intent = new Intent(
                TaskActivity.this,
                DataParsingActivity.class
        );

        startActivity(intent);
    }

    // Open the HTTP Operations screen.
    private void openApi() {

        Intent intent = new Intent(
                TaskActivity.this,
                ApiActivity.class
        );

        startActivity(intent);
    }

    // Open the Firebase Storage screen.
    private void openStorage() {

        Intent intent = new Intent(
                TaskActivity.this,
                StorageActivity.class
        );

        startActivity(intent);
    }

    // Connect dashboard buttons.
    private void setupNavigation() {

        actionAddTask.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    AddTaskActivity.class
            );

            startActivity(intent);
        });

        actionProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    ProgressActivity.class
            );

            startActivity(intent);
        });

        actionFocus.setOnClickListener(v -> openFocusTimer());

        navTasks.setOnClickListener(v -> openTasks());

        tvViewAll.setOnClickListener(v -> openTasks());

        navProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    ProgressActivity.class
            );

            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> openProfile());
    }

    // Add Firebase testing and synchronization buttons.
    private void addFirebaseTestControls() {

        ScrollView root = findViewById(R.id.dashboardScroll);

        if (root == null || root.getChildCount() == 0) {
            return;
        }

        if (!(root.getChildAt(0) instanceof LinearLayout)) {
            return;
        }

        LinearLayout content =
                (LinearLayout) root.getChildAt(0);

        // Prevent duplicate buttons.
        if (content.findViewWithTag("firebase_test_heading") != null) {
            return;
        }

        TextView heading = new TextView(this);

        heading.setTag("firebase_test_heading");
        heading.setText("Firebase Database Testing");
        heading.setTextSize(18);
        heading.setTextColor(0xFF21164B);
        heading.setPadding(0, 24, 0, 12);

        content.addView(heading);

        Button uploadButton = new Button(this);
        uploadButton.setText("Upload Test Task");

        content.addView(uploadButton);

        Button retrieveButton = new Button(this);
        retrieveButton.setText("Retrieve Tasks");

        content.addView(retrieveButton);

        Button syncButton = new Button(this);
        syncButton.setText("Sync Local Tasks to Firebase");

        content.addView(syncButton);

        uploadButton.setOnClickListener(v -> uploadTestTask());

        retrieveButton.setOnClickListener(v -> retrieveTasks());

        syncButton.setOnClickListener(v -> syncLocalTasksToFirebase());
    }

    // Add navigation buttons for the three new features.
    private void addFeatureNavigationControls() {

        ScrollView root = findViewById(R.id.dashboardScroll);

        if (root == null || root.getChildCount() == 0) {
            return;
        }

        if (!(root.getChildAt(0) instanceof LinearLayout)) {
            return;
        }

        LinearLayout content =
                (LinearLayout) root.getChildAt(0);

        // Prevent duplicate feature buttons.
        if (content.findViewWithTag("feature_navigation_heading") != null) {
            return;
        }

        TextView heading = new TextView(this);

        heading.setTag("feature_navigation_heading");
        heading.setText("Additional Features");
        heading.setTextSize(18);
        heading.setTextColor(0xFF21164B);
        heading.setPadding(0, 24, 0, 12);

        content.addView(heading);

        // JSON and XML Parsing button.
        Button parsingButton = new Button(this);
        parsingButton.setText("JSON / XML Parsing");

        content.addView(parsingButton);

        parsingButton.setOnClickListener(v -> openDataParsing());

        // HTTP Operations button.
        Button apiButton = new Button(this);
        apiButton.setText("HTTP Operations");

        content.addView(apiButton);

        apiButton.setOnClickListener(v -> openApi());

        // Firebase Storage button.
        Button storageButton = new Button(this);
        storageButton.setText("Firebase Storage");

        content.addView(storageButton);

        storageButton.setOnClickListener(v -> openStorage());
    }

    // Upload a sample task to Firebase.
    private void uploadTestTask() {

        if (firebaseRepository == null) {

            Toast.makeText(
                    this,
                    "Firebase is not initialized. Please log in.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int testId = (int)
                (System.currentTimeMillis() % 2000000000L) + 1;

        Task testTask = new Task(
                testId,
                "Firebase Test Task",
                "This task was uploaded from TaskMate.",
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date()),
                "10:00",
                "High",
                "Testing",
                "No reminder",
                false
        );

        Toast.makeText(
                this,
                "Uploading test task...",
                Toast.LENGTH_SHORT
        ).show();

        firebaseRepository.uploadTask(
                testTask,
                new FirebaseTaskRepository.OperationCallback() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(
                                TaskActivity.this,
                                "Test task uploaded successfully!",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onFailure(String error) {

                        Toast.makeText(
                                TaskActivity.this,
                                "Upload failed: " + error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // Retrieve tasks from Firebase.
    private void retrieveTasks() {

        if (firebaseRepository == null) {

            Toast.makeText(
                    this,
                    "Firebase is not initialized.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        firebaseRepository.getAllTasks(
                new FirebaseTaskRepository.TasksCallback() {

                    @Override
                    public void onSuccess(List<Task> tasks) {

                        if (tasks.isEmpty()) {

                            Toast.makeText(
                                    TaskActivity.this,
                                    "No cloud tasks found.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        StringBuilder result = new StringBuilder();

                        for (Task task : tasks) {

                            result.append("Title: ")
                                    .append(task.getTitle())
                                    .append("\n");

                            result.append("Priority: ")
                                    .append(task.getPriority())
                                    .append("\n");

                            result.append("Completed: ")
                                    .append(task.isCompleted())
                                    .append("\n\n");
                        }

                        new AlertDialog.Builder(TaskActivity.this)
                                .setTitle(
                                        "Firebase Tasks (" +
                                                tasks.size() + ")"
                                )
                                .setMessage(result.toString())
                                .setPositiveButton("OK", null)
                                .show();
                    }

                    @Override
                    public void onFailure(String error) {

                        Toast.makeText(
                                TaskActivity.this,
                                "Retrieval failed: " + error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // Upload all existing SQLite tasks to Firebase.
    private void syncLocalTasksToFirebase() {

        if (firebaseRepository == null) {

            Toast.makeText(
                    this,
                    "Firebase is not initialized.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Retrieve all tasks from the local SQLite database.
        List<Task> localTasks = database.getAllTasks();

        if (localTasks.isEmpty()) {

            Toast.makeText(
                    this,
                    "No local tasks found.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "Syncing " + localTasks.size() + " tasks...",
                Toast.LENGTH_SHORT
        ).show();

        final int totalTasks = localTasks.size();

        final int[] completedUploads = {0};
        final int[] failedUploads = {0};

        for (Task task : localTasks) {

            firebaseRepository.uploadTask(
                    task,
                    new FirebaseTaskRepository.OperationCallback() {

                        @Override
                        public void onSuccess() {

                            completedUploads[0]++;

                            checkSyncCompletion(
                                    totalTasks,
                                    completedUploads[0],
                                    failedUploads[0]
                            );
                        }

                        @Override
                        public void onFailure(String error) {

                            failedUploads[0]++;

                            checkSyncCompletion(
                                    totalTasks,
                                    completedUploads[0],
                                    failedUploads[0]
                            );
                        }
                    }
            );
        }
    }

    // Display the final synchronization result.
    private void checkSyncCompletion(
            int totalTasks,
            int successfulUploads,
            int failedUploads
    ) {

        if (successfulUploads + failedUploads == totalTasks) {

            String message;

            if (failedUploads == 0) {

                message = successfulUploads +
                        " tasks synced successfully!";

            } else {

                message = successfulUploads + " successful, " +
                        failedUploads + " failed.";
            }

            Toast.makeText(
                    TaskActivity.this,
                    message,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // Update dashboard statistics from SQLite.
    private void updateStatistics() {

        int total = database.getTotalTaskCount();
        int completed = database.getCompletedTaskCount();
        int pending = database.getPendingTaskCount();

        tvTotalTasks.setText(String.valueOf(total));
        tvCompletedTasks.setText(String.valueOf(completed));
        tvPendingTasks.setText(String.valueOf(pending));

        // Calculate overdue tasks.
        int overdue = 0;

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        for (Task task : database.getAllTasks()) {

            if (!task.isCompleted()
                    && task.getDueDate() != null
                    && !task.getDueDate().isEmpty()
                    && task.getDueDate().compareTo(today) < 0) {

                overdue++;
            }
        }

        tvOverdueTasks.setText(String.valueOf(overdue));
    }

    // Refresh dashboard when returning to this screen.
    @Override
    protected void onResume() {
        super.onResume();

        if (database != null) {
            updateStatistics();
            showUserInformation();
        }
    }

    @Override
    protected void onDestroy() {

        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }
}