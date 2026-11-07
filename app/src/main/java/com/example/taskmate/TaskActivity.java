
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    private LinearLayout navTasks;
    private LinearLayout navProgress;

    private TaskDatabase database;

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
    }

    // Connect Java variables with XML views
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

        navTasks = findViewById(R.id.navTasks);
        navProgress = findViewById(R.id.navProgress);
    }

    // Display the logged-in user's name
    private void showUserInformation() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

    // Display today's date
    private void showCurrentDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEEE, MMMM d, yyyy",
                Locale.getDefault()
        );

        tvTodayDate.setText(dateFormat.format(new Date()));
    }

    // Connect dashboard buttons
    private void setupNavigation() {

        // Add Task
        actionAddTask.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    AddTaskActivity.class
            );

            startActivity(intent);
        });

        // View Progress
        actionProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    ProgressActivity.class
            );

            startActivity(intent);
        });

        // Open Task Details
        navTasks.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    TaskDetailsActivity.class
            );

            startActivity(intent);
        });

        // View All Tasks
        tvViewAll.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    TaskDetailsActivity.class
            );

            startActivity(intent);
        });

        // Bottom navigation: Progress
        navProgress.setOnClickListener(v -> {

            Intent intent = new Intent(
                    TaskActivity.this,
                    ProgressActivity.class
            );

            startActivity(intent);
        });
    }

    // Update dashboard statistics from SQLite
    private void updateStatistics() {

        int total = database.getTotalTaskCount();

        int completed = database.getCompletedTaskCount();

        int pending = database.getPendingTaskCount();

        tvTotalTasks.setText(String.valueOf(total));

        tvCompletedTasks.setText(String.valueOf(completed));

        tvPendingTasks.setText(String.valueOf(pending));

        // Calculate overdue tasks
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

    // Refresh dashboard when returning from another screen
    @Override
    protected void onResume() {
        super.onResume();

        if (database != null) {
            updateStatistics();
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
