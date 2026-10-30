
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskActivity extends AppCompatActivity {

    private Button btnAddTask;
    private Button btnProgress;
    private Button btnViewTasks;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Connect Java variables with XML views
        tvWelcome = findViewById(R.id.tvWelcome);
        btnAddTask = findViewById(R.id.btnAddTask);
        btnProgress = findViewById(R.id.btnProgress);
        btnViewTasks = findViewById(R.id.btnViewTasks);

        // Display welcome message
        tvWelcome.setText("Welcome to TaskMate!");

        // Open Add Task screen
        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TaskActivity.this,
                    AddTaskActivity.class
            );
            startActivity(intent);
        });

        // Open Progress screen
        btnProgress.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TaskActivity.this,
                    ProgressActivity.class
            );
            startActivity(intent);
        });

        // Open Task List screen
        btnViewTasks.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TaskActivity.this,
                    TaskDetailsActivity.class
            );
            startActivity(intent);
        });
    }
}
