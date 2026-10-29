
package com.example.taskmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "taskmate.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TASKS = "tasks";

    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DUE_DATE = "due_date";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_COMPLETED = "completed";

    public TaskDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_DUE_DATE + " TEXT NOT NULL, "
                + COL_PRIORITY + " TEXT NOT NULL, "
                + COL_COMPLETED + " INTEGER NOT NULL DEFAULT 0"
                + ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        // Preserve existing data when database changes.
        // Add migration logic here when the schema changes.
    }

    // Add a new task.
    public long addTask(Task task) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.getTitle());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DUE_DATE, task.getDueDate());
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_COMPLETED, task.isCompleted() ? 1 : 0);

        return db.insert(TABLE_TASKS, null, values);
    }

    // Get all saved tasks.
    public List<Task> getAllTasks() {

        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        )) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_ID)
                );

                String title = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_TITLE)
                );

                String description = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DESCRIPTION)
                );

                String dueDate = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_DUE_DATE)
                );

                String priority = cursor.getString(
                        cursor.getColumnIndexOrThrow(COL_PRIORITY)
                );

                boolean completed = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COL_COMPLETED)
                ) == 1;

                Task task = new Task(
                        id,
                        title,
                        description,
                        dueDate,
                        priority,
                        completed
                );

                taskList.add(task);
            }
        }

        return taskList;
    }

    // Update an existing task.
    public int updateTask(Task task) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.getTitle());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DUE_DATE, task.getDueDate());
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_COMPLETED, task.isCompleted() ? 1 : 0);

        return db.update(
                TABLE_TASKS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(task.getId())}
        );
    }

    // Delete a task by its ID.
    public int deleteTask(int taskId) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE_TASKS,
                COL_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    // Mark a task as completed or pending.
    public int updateTaskStatus(int taskId, boolean completed) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_COMPLETED, completed ? 1 : 0);

        return db.update(
                TABLE_TASKS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    // Search tasks by title or description.
    public List<Task> searchTasks(String keyword) {

        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String searchValue = "%" + keyword + "%";

        try (Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                COL_TITLE + " LIKE ? OR " + COL_DESCRIPTION + " LIKE ?",
                new String[]{searchValue, searchValue},
                null,
                null,
                COL_ID + " DESC"
        )) {

            while (cursor.moveToNext()) {

                Task task = new Task(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(COL_ID)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_TITLE)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_DESCRIPTION)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_DUE_DATE)
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COL_PRIORITY)
                        ),
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow(COL_COMPLETED)
                        ) == 1
                );

                taskList.add(task);
            }
        }

        return taskList;
    }

    // Count all tasks.
    public int getTotalTaskCount() {
        return getTaskCount(null);
    }

    // Count completed tasks.
    public int getCompletedTaskCount() {
        return getTaskCount(COL_COMPLETED + " = 1");
    }

    // Count pending tasks.
    public int getPendingTaskCount() {
        return getTaskCount(COL_COMPLETED + " = 0");
    }

    private int getTaskCount(String selection) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{"COUNT(*)"},
                selection,
                null,
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }
}
