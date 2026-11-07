
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TaskDetailsActivity extends AppCompatActivity {

    private TextView tvTaskTitle;
    private TextView tvTaskDescription;
    private TextView tvDueDate;
    private TextView tvPriority;
    private TextView tvTaskStatus;

    // New information fields.
    private TextView tvDueTime;
    private TextView tvCategory;
    private TextView tvReminder;

    private Button btnEditTask;
    private Button btnMarkCompleted;
    private Button btnBack;

    private TaskDatabase database;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        // Initialize existing views.
        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvTaskDescription = findViewById(R.id.tvTaskDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        tvPriority = findViewById(R.id.tvPriority);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);

        // Initialize new views.
        tvDueTime = findViewById(R.id.tvDueTime);
        tvCategory = findViewById(R.id.tvCategory);
        tvReminder = findViewById(R.id.tvReminder);

        btnEditTask = findViewById(R.id.btnEditTask);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
        btnBack = findViewById(R.id.btnBack);

        database = new TaskDatabase(this);

        // Receive selected task ID.
        int taskId = getIntent().getIntExtra("taskId", -1);

        if (taskId == -1) {

            Toast.makeText(
                    this,
                    "Task ID not received.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Retrieve task from SQLite.
        loadTask(taskId);

        // Edit button.
        btnEditTask.setOnClickListener(v -> {

            if (currentTask == null) {
                return;
            }

            Intent editIntent = new Intent(
                    TaskDetailsActivity.this,
                    EditTaskActivity.class
            );

            editIntent.putExtra("taskId", currentTask.getId());

            startActivity(editIntent);
        });

        // Mark task as completed.
        btnMarkCompleted.setOnClickListener(v -> {

            if (currentTask == null) {
                return;
            }

            int result = database.updateTaskStatus(
                    currentTask.getId(),
                    true
            );

            if (result > 0) {

                currentTask.setCompleted(true);

                updateStatusDisplay();

                Toast.makeText(
                        this,
                        "Task marked as completed!",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Unable to update task.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Back button.
        btnBack.setOnClickListener(v -> finish());
    }

    // Retrieve task information from SQLite.
    private void loadTask(int taskId) {

        currentTask = database.getTaskById(taskId);

        if (currentTask == null) {

            Toast.makeText(
                    this,
                    "Task not found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Display title.
        tvTaskTitle.setText(currentTask.getTitle());

        // Display description.
        String description = currentTask.getDescription();

        if (description == null || description.trim().isEmpty()) {
            description = "No description provided";
        }

        tvTaskDescription.setText(description);

        // Display due date.
        tvDueDate.setText(
                "Due Date: " + currentTask.getDueDate()
        );

        // Display due time.
        String dueTime = currentTask.getDueTime();

        if (dueTime == null || dueTime.isEmpty()) {
            dueTime = "Not set";
        }

        tvDueTime.setText("Due Time: " + dueTime);

        // Display priority.
        tvPriority.setText(
                "Priority: " + currentTask.getPriority()
        );

        // Display category.
        String category = currentTask.getCategory();

        if (category == null || category.isEmpty()) {
            category = "Other";
        }

        tvCategory.setText("Category: " + category);

        // Display reminder.
        String reminder = currentTask.getReminder();

        if (reminder == null || reminder.isEmpty()) {
            reminder = "No reminder";
        }

        tvReminder.setText("Reminder: " + reminder);

        updateStatusDisplay();
    }

    // Update task status on screen.
    private void updateStatusDisplay() {

        if (currentTask == null) {
            return;
        }

        if (currentTask.isCompleted()) {

            tvTaskStatus.setText("Status: Completed");

            btnMarkCompleted.setEnabled(false);
            btnMarkCompleted.setText("Task Completed");

        } else {

            tvTaskStatus.setText("Status: Pending");

            btnMarkCompleted.setEnabled(true);
            btnMarkCompleted.setText("Mark as Completed");
        }
    }

    // Refresh task information when returning from Edit Task.
    @Override
    protected void onResume() {
        super.onResume();

        if (database != null && currentTask != null) {
            loadTask(currentTask.getId());
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
