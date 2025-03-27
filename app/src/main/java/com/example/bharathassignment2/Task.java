package com.example.bharathassignment2;

public class Task {
    private String taskId;
    private String taskName;
    private String dueDateTime;

    public Task(String taskId, String taskName, String dueDateTime) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.dueDateTime = dueDateTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDueDateTime() {
        return dueDateTime;
    }
}
