
package com.example.taskmate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle;
    private EditText etTaskDescription;
    private EditText etDueDate;
    private EditText etDueTime;

    private RadioGroup rgPriority;

    private RadioButton rbLow;
    private RadioButton rbMedium;
    private RadioButton rbHigh;

    private Spinner spCategory;
    private Spinner spReminder;

    private Button btnSaveTask;
    private TextView btnBack;

    private TaskDatabase taskDatabase;

    // Stores time in HH:mm format.
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initializeViews();

        taskDatabase = new TaskDatabase(this);

        setupSpinners();
        setupDatePicker();
        setupTimePicker();
        setupPrioritySelection();
        setupButtons();
    }

    // Connect XML components.
    private void initializeViews() {

        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etDueTime = findViewById(R.id.etDueTime);

        rgPriority = findViewById(R.id.rgPriority);

        rbLow = findViewById(R.id.rbLow);
        rbMedium = findViewById(R.id.rbMedium);
        rbHigh = findViewById(R.id.rbHigh);

        spCategory = findViewById(R.id.spCategory);
        spReminder = findViewById(R.id.spReminder);

        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnBack = findViewById(R.id.btnBack);
    }

    // Configure category and reminder options.
    private void setupSpinners() {

        String[] categories = {
                "Personal",
                "Study",
                "Work",
                "Shopping",
                "Health",
                "Other"
        };

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(categoryAdapter);

        String[] reminders = {
                "No reminder",
                "At time of task",
                "5 minutes before",
                "10 minutes before",
                "30 minutes before",
                "1 hour before",
                "1 day before"
        };

        ArrayAdapter<String> reminderAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        reminders
                );

        reminderAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spReminder.setAdapter(reminderAdapter);
    }

    // Open date picker.
    private void setupDatePicker() {

        etDueDate.setFocusable(false);
        etDueDate.setClickable(true);

        etDueDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker =
                    new DatePickerDialog(
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

    // Open time picker.
    private void setupTimePicker() {

        etDueTime.setFocusable(false);
        etDueTime.setClickable(true);

        etDueTime.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker =
                    new TimePickerDialog(
                            this,
                            (view, selectedHour, selectedMinute) -> {

                                // Store time in 24-hour format.
                                selectedTime = String.format(
                                        Locale.US,
                                        "%02d:%02d",
                                        selectedHour,
                                        selectedMinute
                                );

                                // Display time in 12-hour format.
                                Calendar displayTime = Calendar.getInstance();

                                displayTime.set(
                                        Calendar.HOUR_OF_DAY,
                                        selectedHour
                                );

                                displayTime.set(
                                        Calendar.MINUTE,
                                        selectedMinute
                                );

                                String formattedTime =
                                        new SimpleDateFormat(
                                                "hh:mm a",
                                                Locale.getDefault()
                                        ).format(displayTime.getTime());

                                etDueTime.setText(formattedTime);
                            },
                            hour,
                            minute,
                            false
                    );

            timePicker.show();
        });
    }

    // Set up priority selection.
    private void setupPrioritySelection() {

        rbLow.setChecked(true);

        rgPriority.setOnCheckedChangeListener(
                (group, checkedId) -> updatePriorityAppearance()
        );

        updatePriorityAppearance();
    }

    // Update selected and unselected priority text colors.
    private void updatePriorityAppearance() {

        updateSinglePriority(rbLow, 0xFF16803C);
        updateSinglePriority(rbMedium, 0xFFA65C00);
        updateSinglePriority(rbHigh, 0xFFD62828);
    }

    private void updateSinglePriority(
            RadioButton button,
            int normalTextColor
    ) {

        if (button.isChecked()) {

            button.setTextColor(0xFF211936);
            button.setElevation(6f);

        } else {

            button.setTextColor(normalTextColor);
            button.setElevation(0f);
        }

        button.refreshDrawableState();
    }

    // Configure button actions.
    private void setupButtons() {

        btnSaveTask.setOnClickListener(v -> saveTask());

        btnBack.setOnClickListener(v -> finish());
    }

    // Get selected priority.
    private String getSelectedPriority() {

        int selectedId = rgPriority.getCheckedRadioButtonId();

        if (selectedId == R.id.rbHigh) {
            return "High";
        }

        if (selectedId == R.id.rbMedium) {
            return "Medium";
        }

        return "Low";
    }

    // Validate and save task.
    private void saveTask() {

        String title = etTaskTitle.getText().toString().trim();

        String description =
                etTaskDescription.getText().toString().trim();

        String dueDate = etDueDate.getText().toString().trim();

        String dueTime = selectedTime;

        String priority = getSelectedPriority();

        String category =
                spCategory.getSelectedItem().toString();

        String reminder =
                spReminder.getSelectedItem().toString();

        // Validate task title.
        if (title.isEmpty()) {

            etTaskTitle.setError("Enter task title");
            etTaskTitle.requestFocus();
            return;
        }

        // Validate due date.
        if (dueDate.isEmpty()) {

            etDueDate.setError("Select due date");
            etDueDate.requestFocus();
            return;
        }

        // Create Task object with all information.
        Task task = new Task(
                title,
                description,
                dueDate,
                dueTime,
                priority,
                category,
                reminder
        );

        // Save task in SQLite.
        long result = taskDatabase.addTask(task);

        if (result != -1) {

            Toast.makeText(
                    this,
                    "Task saved successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();

        } else {

            Toast.makeText(
                    this,
                    "Unable to save task.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {

        if (taskDatabase != null) {
            taskDatabase.close();
        }

        super.onDestroy();
    }
}
