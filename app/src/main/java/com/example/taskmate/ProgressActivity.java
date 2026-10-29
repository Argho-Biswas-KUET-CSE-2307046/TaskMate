
package com.example.taskmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvTotalTasks;
    private TextView tvCompletedTasks;
    private TextView tvPendingTasks;
    private TextView tvCompletionPercentage;
    private ProgressBar progressBarTasks;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        tvCompletionPercentage =
                findViewById(R.id.tvCompletionPercentage);

        progressBarTasks = findViewById(R.id.progressBarTasks);
        btnBack = findViewById(R.id.btnBack);

        // Receive progress values from the previous screen.
        int totalTasks = getIntent().getIntExtra("totalTasks", 0);
        int completedTasks =
                getIntent().getIntExtra("completedTasks", 0);

        // Prevent invalid values.
        totalTasks = Math.max(0, totalTasks);
        completedTasks = Math.max(
                0,
                Math.min(completedTasks, totalTasks)
        );

        int pendingTasks = totalTasks - completedTasks;

        int percentage = totalTasks == 0
                ? 0
                : (int) ((completedTasks * 100.0) / totalTasks);

        // Display the progress.
        tvTotalTasks.setText("Total Tasks: " + totalTasks);
        tvCompletedTasks.setText(
                "Completed Tasks: " + completedTasks
        );
        tvPendingTasks.setText(
                "Pending Tasks: " + pendingTasks
        );

        tvCompletionPercentage.setText(
                "Completion: " + percentage + "%"
        );

        progressBarTasks.setMax(100);
        progressBarTasks.setProgress(percentage);

        btnBack.setOnClickListener(v -> finish());
    }
}
