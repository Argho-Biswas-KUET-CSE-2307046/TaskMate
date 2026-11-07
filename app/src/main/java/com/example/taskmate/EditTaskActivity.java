
package com.example.taskmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private EditText etDueDate;
    private EditText etDueTime;

    private Spinner spPriority;
    private Spinner spCategory;
    private Spinner spReminder;

    private Button btnUpdateTask;
    private Button btnCancelEdit;

    private TextView tvEditBack;

    private TaskDatabase database;
    private Task currentTask;

    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        initializeViews();

        database = new TaskDatabase(this);

        setupSpinners();
        setupDatePicker();
        setupTimePicker();

        // Receive the selected task ID.
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

        loadTask(taskId);

        // Back arrow.
        tvEditBack.setOnClickListener(v -> finish());

        // Update button.
        btnUpdateTask.setOnClickListener(v -> updateTask());

        // Cancel button.
        btnCancelEdit.setOnClickListener(v -> finish());
    }

    // Connect Java variables with XML views.
    private void initializeViews() {

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etDueTime = findViewById(R.id.etDueTime);

        spPriority = findViewById(R.id.spPriority);
        spCategory = findViewById(R.id.spCategory);
        spReminder = findViewById(R.id.spReminder);

        btnUpdateTask = findViewById(R.id.btnUpdateTask);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        tvEditBack = findViewById(R.id.tvEditBack);
    }

    // Configure dropdown menus.
    private void setupSpinners() {

        setSpinnerAdapter(
                spPriority,
                new String[]{"Low", "Medium", "High"}
        );

        setSpinnerAdapter(
                spCategory,
                new String[]{
                        "Personal",
                        "Study",
                        "Work",
                        "Shopping",
                        "Health",
                        "Other"
                }
        );

        setSpinnerAdapter(
                spReminder,
                new String[]{
                        "No reminder",
                        "At time of task",
                        "5 minutes before",
                        "10 minutes before",
                        "30 minutes before",
                        "1 hour before",
                        "1 day before"
                }
        );
    }

    // Attach an adapter to a Spinner.
    private void setSpinnerAdapter(
            Spinner spinner,
            String[] items
    ) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);
    }

    // Select a dropdown value safely.
    private void selectSpinnerItem(
            Spinner spinner,
            String value
    ) {

        if (value == null) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {

            Object item = spinner.getItemAtPosition(i);

            if (item != null &&
                    item.toString().equalsIgnoreCase(value)) {

                spinner.setSelection(i);
                return;
            }
        }
    }

    // Load task information from SQLite.
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

        etTaskTitle.setText(currentTask.getTitle());

        etTaskDescription.setText(
                currentTask.getDescription()
        );

        etDueDate.setText(currentTask.getDueDate());

        selectSpinnerItem(
                spPriority,
                currentTask.getPriority()
        );

        // These fields are not stored in the current database.
        selectSpinnerItem(spCategory, "Other");
        selectSpinnerItem(spReminder, "No reminder");

        etDueTime.setText("");
    }

    // Configure date picker.
    private void setupDatePicker() {

        etDueDate.setFocusable(false);

        etDueDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {

                        String date = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                day
                        );

                        etDueDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.getDatePicker().setMinDate(
                    System.currentTimeMillis() - 1000
            );

            datePicker.show();
        });
    }

    // Configure time picker.
    private void setupTimePicker() {

        etDueTime.setFocusable(false);

        etDueTime.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            TimePickerDialog timePicker = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {

                        selectedTime = String.format(
                                Locale.US,
                                "%02d:%02d",
                                hour,
                                minute
                        );

                        Calendar displayTime = Calendar.getInstance();

                        displayTime.set(
                                Calendar.HOUR_OF_DAY,
                                hour
                        );

                        displayTime.set(
                                Calendar.MINUTE,
                                minute
                        );

                        String formattedTime =
                                new java.text.SimpleDateFormat(
                                        "hh:mm a",
                                        Locale.getDefault()
                                ).format(displayTime.getTime());

                        etDueTime.setText(formattedTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );

            timePicker.show();
        });
    }

    // Update the existing task.
    private void updateTask() {

        if (currentTask == null) {
            return;
        }

        String title = etTaskTitle.getText()
                .toString()
                .trim();

        String description = etTaskDescription.getText()
                .toString()
                .trim();

        String dueDate = etDueDate.getText()
                .toString()
                .trim();

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

        // Update existing Task object.
        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDueDate(dueDate);
        currentTask.setPriority(priority);

        // Save changes to SQLite.
        int result = database.updateTask(currentTask);

        if (result > 0) {

            Toast.makeText(
                    this,
                    "Task updated successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Unable to update task.",
                    Toast.LENGTH_LONG
            ).show();
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
