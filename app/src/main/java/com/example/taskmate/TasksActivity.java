
package com.example.taskmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TasksActivity extends AppCompatActivity {

    private EditText etSearchTasks;

    private Button btnAll;
    private Button btnPending;
    private Button btnCompleted;

    private TextView btnBack;
    private TextView tvEmptyTasks;

    private RecyclerView rvTasks;
    private FloatingActionButton fabAddTask;

    private TaskDatabase database;
    private TaskAdapter taskAdapter;

    private List<Task> allTasks = new ArrayList<>();

    private String currentFilter = "All";
    private String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        database = new TaskDatabase(this);

        initializeViews();
        setupRecyclerView();
        setupButtons();
        setupSearch();

        loadTasks();
    }

    // Connect Java variables with XML views
    private void initializeViews() {

        etSearchTasks = findViewById(R.id.etSearchTasks);

        btnAll = findViewById(R.id.btnAll);
        btnPending = findViewById(R.id.btnPending);
        btnCompleted = findViewById(R.id.btnCompleted);

        btnBack = findViewById(R.id.btnBack);
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);

        rvTasks = findViewById(R.id.rvTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
    }

    // Set up the RecyclerView
    private void setupRecyclerView() {

        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(
                this,
                new ArrayList<>(),
                this::loadTasks
        );

        rvTasks.setAdapter(taskAdapter);
    }

    // Set up button actions
    private void setupButtons() {

        btnBack.setOnClickListener(v -> finish());

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TasksActivity.this,
                    AddTaskActivity.class
            );
            startActivity(intent);
        });

        btnAll.setOnClickListener(v -> {
            currentFilter = "All";
            updateFilterButtons();
            applyFilters();
        });

        btnPending.setOnClickListener(v -> {
            currentFilter = "Pending";
            updateFilterButtons();
            applyFilters();
        });

        btnCompleted.setOnClickListener(v -> {
            currentFilter = "Completed";
            updateFilterButtons();
            applyFilters();
        });

        updateFilterButtons();
    }

    // Search tasks by title
    private void setupSearch() {

        etSearchTasks.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                searchKeyword = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Load tasks from SQLite database
    private void loadTasks() {

        allTasks = database.getAllTasks();

        if (allTasks == null) {
            allTasks = new ArrayList<>();
        }

        applyFilters();
    }

    // Apply status filter and search keyword
    private void applyFilters() {

        List<Task> filteredTasks = new ArrayList<>();

        for (Task task : allTasks) {

            boolean matchesStatus;

            if (currentFilter.equals("Pending")) {
                matchesStatus = !task.isCompleted();
            } else if (currentFilter.equals("Completed")) {
                matchesStatus = task.isCompleted();
            } else {
                matchesStatus = true;
            }

            String title = task.getTitle() == null
                    ? ""
                    : task.getTitle().toLowerCase(Locale.ROOT);

            boolean matchesSearch = title.contains(
                    searchKeyword.toLowerCase(Locale.ROOT)
            );

            if (matchesStatus && matchesSearch) {
                filteredTasks.add(task);
            }
        }

        taskAdapter.updateTasks(filteredTasks);

        // Show empty message if no tasks match
        if (filteredTasks.isEmpty()) {
            tvEmptyTasks.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            tvEmptyTasks.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }
    }

    // Highlight the selected filter
    private void updateFilterButtons() {

        int selectedColor = 0xFF7132CF;
        int normalColor = 0xFFEDE5FF;

        int selectedTextColor = 0xFFFFFFFF;
        int normalTextColor = 0xFF35245B;

        setFilterButtonStyle(
                btnAll,
                currentFilter.equals("All"),
                selectedColor,
                normalColor,
                selectedTextColor,
                normalTextColor
        );

        setFilterButtonStyle(
                btnPending,
                currentFilter.equals("Pending"),
                selectedColor,
                normalColor,
                selectedTextColor,
                normalTextColor
        );

        setFilterButtonStyle(
                btnCompleted,
                currentFilter.equals("Completed"),
                selectedColor,
                normalColor,
                selectedTextColor,
                normalTextColor
        );
    }

    private void setFilterButtonStyle(
            Button button,
            boolean selected,
            int selectedColor,
            int normalColor,
            int selectedTextColor,
            int normalTextColor
    ) {

        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        selected ? selectedColor : normalColor
                )
        );

        button.setTextColor(
                selected ? selectedTextColor : normalTextColor
        );
    }

    // Refresh tasks when returning from Add Task screen
    @Override
    protected void onResume() {
        super.onResume();

        if (database != null && taskAdapter != null) {
            loadTasks();
        }
    }

    @Override
    protected void onDestroy() {

        if (taskAdapter != null) {
            taskAdapter.closeDatabase();
        }

        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }
}
