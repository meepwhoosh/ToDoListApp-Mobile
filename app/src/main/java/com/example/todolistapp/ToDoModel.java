package com.example.todolistapp;

public class ToDoModel {
    private int id;
    private String taskName;
    private String taskDescription;
    private String dueDate;
    private String dueTime;
    private String label;
    private int status; // 0 for ongoing, 1 for completed
    private int userId;

    public ToDoModel(int id, String taskName, String taskDescription, String dueDate, String dueTime, String label, int status, int userId) {
        this.id = id;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.label = label;
        this.status = status;
        this.userId = userId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public String getLabel() {
        return label;
    }

    public int getStatus() {
        return status;
    }

    public int getUserId() {
        return userId;
    }
}
