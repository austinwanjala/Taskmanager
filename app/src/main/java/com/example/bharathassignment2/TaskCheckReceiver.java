package com.example.bharathassignment2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Map;

public class TaskCheckReceiver extends BroadcastReceiver {
    private static final String TASK_PREFS = "Tasks";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE);
        long currentTime = System.currentTimeMillis();
        long oneHourLater = currentTime + 3600000; // 1 hour later

        Map<String, ?> tasks = prefs.getAll();
        for (Map.Entry<String, ?> entry : tasks.entrySet()) {
            String taskId = entry.getKey();
            long taskDueTime = prefs.getLong(taskId + "_dueTime", 0);

            if (taskDueTime > currentTime && taskDueTime <= oneHourLater) {
                TaskNotificationReceiver.sendTaskReminder(context, taskId, entry.getValue().toString());
            }
        }
    }
}

