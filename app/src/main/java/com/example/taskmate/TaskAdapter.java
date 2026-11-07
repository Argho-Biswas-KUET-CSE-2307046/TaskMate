
package com.example.taskmate;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> taskList;
    private final TaskDatabase database;

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged();
    }

    private final OnTaskStatusChangedListener listener;

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
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_task,
                parent,
                false
        );

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position
    ) {
        Task task = taskList.get(position);

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDueDate.setText(formatDate(task.getDueDate()));
        holder.tvTaskPriority.setText(task.getPriority());

        // Set priority color
        String priority = task.getPriority().toLowerCase(Locale.ROOT);

        int priorityColor;
        int priorityTextColor;

        switch (priority) {
            case "high":
                priorityColor = Color.parseColor("#FFE1E1");
                priorityTextColor = Color.parseColor("#D62828");
                break;

            case "medium":
                priorityColor = Color.parseColor("#FFF0D1");
                priorityTextColor = Color.parseColor("#A65C00");
                break;

            default:
                priorityColor = Color.parseColor("#DDF5E4");
                priorityTextColor = Color.parseColor("#16803C");
                break;
        }

        holder.tvTaskPriority.setBackgroundTintList(
                ColorStateList.valueOf(priorityColor)
        );

        holder.tvTaskPriority.setTextColor(priorityTextColor);

        // Set completed status
        holder.cbTaskCompleted.setOnCheckedChangeListener(null);

        holder.cbTaskCompleted.setChecked(task.isCompleted());

        holder.tvTaskTitle.setAlpha(
                task.isCompleted() ? 0.55f : 1.0f
        );

        // Update task status in SQLite
        holder.cbTaskCompleted.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    int result = database.updateTaskStatus(
                            task.getId(),
                            isChecked
                    );

                    if (result > 0) {

                        task.setCompleted(isChecked);

                        holder.tvTaskTitle.setAlpha(
                                isChecked ? 0.55f : 1.0f
                        );

                        if (listener != null) {
                            listener.onTaskStatusChanged();
                        }
                    } else {
                        holder.cbTaskCompleted.setOnCheckedChangeListener(null);
                        holder.cbTaskCompleted.setChecked(!isChecked);
                        holder.cbTaskCompleted.setOnCheckedChangeListener(
                                (button, checked) -> {
                                    database.updateTaskStatus(
                                            task.getId(),
                                            checked
                                    );
                                }
                        );
                    }
                }
        );
    }

    private String formatDate(String dateString) {

        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            Date date = inputFormat.parse(dateString);

            if (date != null) {
                SimpleDateFormat outputFormat =
                        new SimpleDateFormat("EEE, MMM d", Locale.US);

                return outputFormat.format(date);
            }

        } catch (ParseException | NullPointerException e) {
            // Return the original date if formatting fails.
        }

        return dateString;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged();
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

    public void closeDatabase() {
        database.close();
    }
}
