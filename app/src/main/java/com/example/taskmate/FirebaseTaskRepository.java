
package com.example.taskmate;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseTaskRepository {

    // Callback for upload, update and delete operations.
    public interface OperationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Callback for retrieving tasks.
    public interface TasksCallback {
        void onSuccess(List<Task> tasks);
        void onFailure(String error);
    }

    private final DatabaseReference tasksReference;

    // Firebase database URL.
    private static final String DATABASE_URL =
            "https://taskmate-a7c9b-default-rtdb.asia-southeast1.firebasedatabase.app";

    // Constructor.
    public FirebaseTaskRepository() {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            throw new IllegalStateException(
                    "User must be logged in before accessing cloud tasks."
            );
        }

        String userId = user.getUid();

        tasksReference = FirebaseDatabase
                .getInstance(DATABASE_URL)
                .getReference("users")
                .child(userId)
                .child("tasks");
    }

    // Convert Task object into Firebase-compatible data.
    private Map<String, Object> taskToMap(Task task) {

        Map<String, Object> data = new HashMap<>();

        data.put("id", task.getId());
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("dueDate", task.getDueDate());
        data.put("dueTime", task.getDueTime());
        data.put("priority", task.getPriority());
        data.put("category", task.getCategory());
        data.put("reminder", task.getReminder());
        data.put("completed", task.isCompleted());

        return data;
    }

    // Upload a new task.
    public void uploadTask(
            Task task,
            OperationCallback callback
    ) {

        if (task == null || task.getId() <= 0) {
            callback.onFailure("Invalid task or task ID.");
            return;
        }

        String taskId = String.valueOf(task.getId());

        tasksReference.child(taskId)
                .setValue(taskToMap(task))
                .addOnSuccessListener(unused -> {

                    if (callback != null) {
                        callback.onSuccess();
                    }

                })
                .addOnFailureListener(error -> {

                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }

                });
    }

    // Retrieve all tasks belonging to the current user.
    public void getAllTasks(TasksCallback callback) {

        tasksReference.get()
                .addOnSuccessListener(snapshot -> {

                    List<Task> taskList = new ArrayList<>();

                    for (DataSnapshot item : snapshot.getChildren()) {

                        Task task = item.getValue(Task.class);

                        if (task != null) {
                            taskList.add(task);
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(taskList);
                    }

                })
                .addOnFailureListener(error -> {

                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }

                });
    }

    // Update an existing task.
    public void updateTask(
            Task task,
            OperationCallback callback
    ) {

        if (task == null || task.getId() <= 0) {
            callback.onFailure("Invalid task or task ID.");
            return;
        }

        String taskId = String.valueOf(task.getId());

        tasksReference.child(taskId)
                .updateChildren(taskToMap(task))
                .addOnSuccessListener(unused -> {

                    if (callback != null) {
                        callback.onSuccess();
                    }

                })
                .addOnFailureListener(error -> {

                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }

                });
    }

    // Delete a task.
    public void deleteTask(
            int taskId,
            OperationCallback callback
    ) {

        if (taskId <= 0) {
            callback.onFailure("Invalid task ID.");
            return;
        }

        tasksReference.child(String.valueOf(taskId))
                .removeValue()
                .addOnSuccessListener(unused -> {

                    if (callback != null) {
                        callback.onSuccess();
                    }

                })
                .addOnFailureListener(error -> {

                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }

                });
    }

    // Listen for real-time changes in Firebase.
    public ValueEventListener listenForTasks(
            TasksCallback callback
    ) {

        ValueEventListener listener = new ValueEventListener() {

            @Override
            public void onDataChange(
                    @NonNull DataSnapshot snapshot
            ) {

                List<Task> taskList = new ArrayList<>();

                for (DataSnapshot item : snapshot.getChildren()) {

                    Task task = item.getValue(Task.class);

                    if (task != null) {
                        taskList.add(task);
                    }
                }

                if (callback != null) {
                    callback.onSuccess(taskList);
                }
            }

            @Override
            public void onCancelled(
                    @NonNull DatabaseError error
            ) {

                if (callback != null) {
                    callback.onFailure(error.getMessage());
                }
            }
        };

        tasksReference.addValueEventListener(listener);

        return listener;
    }

    // Remove listener when no longer needed.
    public void removeListener(ValueEventListener listener) {

        if (listener != null) {
            tasksReference.removeEventListener(listener);
        }
    }
}
