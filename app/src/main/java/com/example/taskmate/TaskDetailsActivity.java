
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

    private Button btnEditTask;
    private Button btnMarkCompleted;
    private Button btnBack;

    private String taskTitle;
    private String taskDescription;
    private String dueDate;
    private String priority;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        tvTaskTitle = findViewById(R.id.tvTaskTitle);
        tvTaskDescription = findViewById(R.id.tvTaskDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        tvPriority = findViewById(R.id.tvPriority);
        tvTaskStatus = findViewById(R.id.tvTaskStatus);

        btnEditTask = findViewById(R.id.btnEditTask);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
        btnBack = findViewById(R.id.btnBack);

        // Receive task details from the previous screen.
        Intent intent = getIntent();

        taskTitle = intent.getStringExtra("taskTitle");
        taskDescription = intent.getStringExtra("taskDescription");
        dueDate = intent.getStringExtra("dueDate");
        priority = intent.getStringExtra("priority");
        status = intent.getStringExtra("status");

        // Display the task details.
        tvTaskTitle.setText(
                taskTitle == null ? "Untitled Task" : taskTitle
        );

        tvTaskDescription.setText(
                taskDescription == null || taskDescription.isEmpty()
                        ? "No description provided"
                        : taskDescription
        );

        tvDueDate.setText(
                "Due Date: " +
                        (dueDate == null ? "Not set" : dueDate)
        );

        tvPriority.setText(
                "Priority: " +
                        (priority == null ? "Medium" : priority)
        );

        if (status == null || status.isEmpty()) {
            status = "Pending";
        }

        updateStatusDisplay();

        btnEditTask.setOnClickListener(v -> {
            Intent editIntent = new Intent(
                    TaskDetailsActivity.this,
                    EditTaskActivity.class
            );

            editIntent.putExtra("taskTitle", taskTitle);
            editIntent.putExtra("taskDescription", taskDescription);
            editIntent.putExtra("dueDate", dueDate);
            editIntent.putExtra("priority", priority);
            editIntent.putExtra("status", status);

            startActivity(editIntent);
        });

        btnMarkCompleted.setOnClickListener(v -> {
            status = "Completed";
            updateStatusDisplay();

            Toast.makeText(
                    TaskDetailsActivity.this,
                    "Status changed to Completed.",
                    Toast.LENGTH_SHORT
            ).show();

            // Later, update the task in the database.
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateStatusDisplay() {
        tvTaskStatus.setText("Status: " + status);

        if ("Completed".equals(status)) {
            btnMarkCompleted.setEnabled(false);
            btnMarkCompleted.setText("Task Completed");
        } else {
            btnMarkCompleted.setEnabled(true);
            btnMarkCompleted.setText("Mark as Completed");
        }
    }
}
