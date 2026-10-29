
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

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private EditText etDueDate;
    private Spinner spPriority;
    private Button btnSaveTask, btnCancelTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        spPriority = findViewById(R.id.spPriority);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnCancelTask = findViewById(R.id.btnCancelTask);

        // Set the available task priorities.
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

        // Open the date picker when the date field is clicked.
        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);
        etDueDate.setOnClickListener(v -> showDatePicker());

        btnSaveTask.setOnClickListener(v -> saveTask());

        btnCancelTask.setOnClickListener(v -> finish());
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

    private void saveTask() {
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

        Toast.makeText(
                this,
                "Task details validated! Database saving will be added next.",
                Toast.LENGTH_LONG
        ).show();

        // Later, save title, description, dueDate and priority
        // in the local database and synchronize with the cloud.
    }
}
