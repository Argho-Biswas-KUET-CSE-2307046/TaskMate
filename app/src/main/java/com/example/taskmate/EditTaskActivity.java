
package com.example.taskmate;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private EditText etDueDate;
    private Spinner spPriority;
    private Button btnUpdateTask, btnCancelEdit;

    private String originalStatus = "Pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        spPriority = findViewById(R.id.spPriority);
        btnUpdateTask = findViewById(R.id.btnUpdateTask);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        // Set priority options.
        String[] priorities = {"Low", "Medium", "High"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                priorities
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spPriority.setAdapter(adapter);

        // Receive existing task details.
        String title = getIntent().getStringExtra("taskTitle");
        String description =
                getIntent().getStringExtra("taskDescription");
        String dueDate = getIntent().getStringExtra("dueDate");
        String priority = getIntent().getStringExtra("priority");
        String status = getIntent().getStringExtra("status");

        if (title != null) {
            etTaskTitle.setText(title);
        }

        if (description != null) {
            etTaskDescription.setText(description);
        }

        if (dueDate != null) {
            etDueDate.setText(dueDate);
        }

        if (status != null && !status.isEmpty()) {
            originalStatus = status;
        }

        if (priority != null) {
            int position = adapter.getPosition(priority);
            if (position >= 0) {
                spPriority.setSelection(position);
            }
        }

        // Open the date picker.
        etDueDate.setFocusable(false);
        etDueDate.setOnClickListener(v -> showDatePicker());

        btnUpdateTask.setOnClickListener(v -> updateTask());

        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                    );

                    etDueDate.setText(date);
                },
                year,
                month,
                day
        );

        datePicker.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        datePicker.show();
    }

    private void updateTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description =
                etTaskDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String priority = spPriority.getSelectedItem().toString();

        if (title.isEmpty()) {
            etTaskTitle.setError("Enter a task title");
            etTaskTitle.requestFocus();
            return;
        }

        if (dueDate.isEmpty()) {
            etDueDate.setError("Select a due date");
            etDueDate.requestFocus();
            return;
        }

        // Return updated details to TaskDetailsActivity.
        android.content.Intent result =
                new android.content.Intent();

        result.putExtra("taskTitle", title);
        result.putExtra("taskDescription", description);
        result.putExtra("dueDate", dueDate);
        result.putExtra("priority", priority);
        result.putExtra("status", originalStatus);

        setResult(RESULT_OK, result);

        Toast.makeText(
                this,
                "Task details updated for this session.",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
