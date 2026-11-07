
package com.example.taskmate;

public class Task {

    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime;
    private String priority;
    private String category;
    private String reminder;
    private boolean completed;

    // Constructor for creating a new task.
    public Task(String title, String description,
                String dueDate, String priority) {

        this(title, description, dueDate, "",
                priority, "Other", "No reminder");
    }

    // New constructor with all task information.
    public Task(String title, String description,
                String dueDate, String dueTime,
                String priority, String category,
                String reminder) {

        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority;
        this.category = category;
        this.reminder = reminder;
        this.completed = false;
    }

    // Constructor for loading an existing task.
    public Task(int id, String title, String description,
                String dueDate, String priority,
                boolean completed) {

        this(id, title, description, dueDate, "",
                priority, "Other", "No reminder", completed);
    }

    // New constructor for loading all saved task information.
    public Task(int id, String title, String description,
                String dueDate, String dueTime,
                String priority, String category,
                String reminder, boolean completed) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority;
        this.category = category;
        this.reminder = reminder;
        this.completed = completed;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public String getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getReminder() {
        return reminder;
    }

    public boolean isCompleted() {
        return completed;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
