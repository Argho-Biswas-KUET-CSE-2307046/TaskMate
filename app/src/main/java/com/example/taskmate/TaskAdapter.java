
package com.example.taskmate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> taskList;
    private final TaskDatabase database;
    private final OnTaskStatusChangedListener listener;

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged();
    }

    public TaskAdapter(
            Context context,
            List<Task> taskList,
            OnTaskStatusChangedListener listener
    ) {

        this.context = context;
        this.taskList = new ArrayList<>(taskList);
        this.listener = listener;

        this.database = new TaskDatabase(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position
    ) {

        Task task = taskList.get(position);

        // Display task title.
        holder.tvTaskTitle.setText(task.getTitle());

        // Build task information.
        StringBuilder taskInfo = new StringBuilder();

        taskInfo.append("Due: ")
                .append(task.getDueDate());

        // Display due time if available.
        if (task.getDueTime() != null
                && !task.getDueTime().isEmpty()) {

            taskInfo.append(" • ")
                    .append(task.getDueTime());
        }

        // Display category.
        if (task.getCategory() != null
                && !task.getCategory().isEmpty()) {

            taskInfo.append(" • ")
                    .append(task.getCategory());
        }

        holder.tvTaskDueDate.setText(taskInfo.toString());

        // Display priority.
        String priority = task.getPriority();

        holder.tvTaskPriority.setText(priority);

        // Set priority background color.
        int priorityColor;

        switch (priority.toLowerCase()) {

            case "high":
                priorityColor = 0xFFFFDCDC;
                break;

            case "low":
                priorityColor = 0xFFDDF5E5;
                break;

            default:
                priorityColor = 0xFFFFF0D2;
                break;
        }

        holder.tvTaskPriority.setBackgroundTintList(
                ColorStateList.valueOf(priorityColor)
        );

        // Display completion status.
        holder.cbTaskCompleted.setOnCheckedChangeListener(null);

        holder.cbTaskCompleted.setChecked(task.isCompleted());

        holder.cbTaskCompleted.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    int result = database.updateTaskStatus(
                            task.getId(),
                            isChecked
                    );

                    if (result > 0) {

                        task.setCompleted(isChecked);

                        if (isChecked) {

                            // Cancel reminder when task is completed.
                            TaskReminderScheduler.cancelReminder(
                                    context,
                                    task.getId()
                            );

                        } else {

                            // Schedule reminder again if task becomes pending.
                            TaskReminderScheduler.scheduleReminder(
                                    context,
                                    task
                            );
                        }

                        if (listener != null) {
                            listener.onTaskStatusChanged();
                        }
                    }
                }
        );

        // Open details of the selected task.
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    TaskDetailsActivity.class
            );

            intent.putExtra("taskId", task.getId());

            context.startActivity(intent);
        });

        // Delete task using long press.
        holder.itemView.setOnLongClickListener(v -> {

            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return true;
            }

            Task selectedTask = taskList.get(currentPosition);

            showDeleteConfirmation(selectedTask);

            return true;
        });
    }

    // Show confirmation before deleting a task.
    private void showDeleteConfirmation(Task task) {

        new AlertDialog.Builder(context)
                .setTitle("Delete Task")
                .setMessage(
                        "Are you sure you want to delete \""
                                + task.getTitle() + "\"?"
                )
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Delete", (dialog, which) -> {

                    // Delete from SQLite and cancel scheduled reminder.
                    int result = database.deleteTask(task.getId());

                    if (result > 0) {

                        // Find the task in the adapter list.
                        int deletedPosition = -1;

                        for (int i = 0; i < taskList.size(); i++) {

                            if (taskList.get(i).getId() == task.getId()) {
                                deletedPosition = i;
                                break;
                            }
                        }

                        // Remove task from RecyclerView.
                        if (deletedPosition != -1) {

                            taskList.remove(deletedPosition);

                            notifyItemRemoved(deletedPosition);
                        }

                        Toast.makeText(
                                context,
                                "Task deleted successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        if (listener != null) {
                            listener.onTaskStatusChanged();
                        }

                    } else {

                        Toast.makeText(
                                context,
                                "Unable to delete task.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Replace the displayed task list.
    public void updateTasks(List<Task> newTasks) {

        taskList.clear();
        taskList.addAll(newTasks);

        notifyDataSetChanged();
    }

    public void closeDatabase() {
        database.close();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView tvTaskTitle;
        TextView tvTaskDueDate;
        TextView tvTaskPriority;
        CheckBox cbTaskCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            tvTaskPriority = itemView.findViewById(R.id.tvTaskPriority);
            cbTaskCompleted = itemView.findViewById(R.id.cbTaskCompleted);
        }
    }
}
