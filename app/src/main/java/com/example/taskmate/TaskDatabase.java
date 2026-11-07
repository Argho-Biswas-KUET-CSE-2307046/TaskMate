
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

    // Increased from version 1 to version 2.
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_TASKS = "tasks";

    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DUE_DATE = "due_date";
    private static final String COL_DUE_TIME = "due_time";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_CATEGORY = "category";
    private static final String COL_REMINDER = "reminder";
    private static final String COL_COMPLETED = "completed";

    public TaskDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the database for new installations.
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_DUE_DATE + " TEXT NOT NULL, "
                + COL_DUE_TIME + " TEXT NOT NULL DEFAULT '', "
                + COL_PRIORITY + " TEXT NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL DEFAULT 'Other', "
                + COL_REMINDER + " TEXT NOT NULL DEFAULT 'No reminder', "
                + COL_COMPLETED + " INTEGER NOT NULL DEFAULT 0"
                + ")";

        db.execSQL(createTable);
    }

    // Upgrade an existing database without deleting saved tasks.
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        if (oldVersion < 2) {

            db.execSQL(
                    "ALTER TABLE " + TABLE_TASKS
                            + " ADD COLUMN " + COL_DUE_TIME
                            + " TEXT NOT NULL DEFAULT ''"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_TASKS
                            + " ADD COLUMN " + COL_CATEGORY
                            + " TEXT NOT NULL DEFAULT 'Other'"
            );

            db.execSQL(
                    "ALTER TABLE " + TABLE_TASKS
                            + " ADD COLUMN " + COL_REMINDER
                            + " TEXT NOT NULL DEFAULT 'No reminder'"
            );
        }
    }

    // Add a new task.
    public long addTask(Task task) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_TITLE, task.getTitle());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DUE_DATE, task.getDueDate());
        values.put(COL_DUE_TIME, task.getDueTime());
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_CATEGORY, task.getCategory());
        values.put(COL_REMINDER, task.getReminder());
        values.put(COL_COMPLETED, task.isCompleted() ? 1 : 0);

        return db.insert(TABLE_TASKS, null, values);
    }

    // Convert a database row into a Task object.
    private Task cursorToTask(Cursor cursor) {

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

        String dueTime = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_DUE_TIME)
        );

        String priority = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_PRIORITY)
        );

        String category = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_CATEGORY)
        );

        String reminder = cursor.getString(
                cursor.getColumnIndexOrThrow(COL_REMINDER)
        );

        boolean completed = cursor.getInt(
                cursor.getColumnIndexOrThrow(COL_COMPLETED)
        ) == 1;

        return new Task(
                id,
                title,
                description,
                dueDate,
                dueTime,
                priority,
                category,
                reminder,
                completed
        );
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
                taskList.add(cursorToTask(cursor));
            }
        }

        return taskList;
    }

    // Get one task using its ID.
    public Task getTaskById(int taskId) {

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                return cursorToTask(cursor);
            }
        }

        return null;
    }

    // Update an existing task.
    public int updateTask(Task task) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_TITLE, task.getTitle());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DUE_DATE, task.getDueDate());
        values.put(COL_DUE_TIME, task.getDueTime());
        values.put(COL_PRIORITY, task.getPriority());
        values.put(COL_CATEGORY, task.getCategory());
        values.put(COL_REMINDER, task.getReminder());
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
                taskList.add(cursorToTask(cursor));
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
