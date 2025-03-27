package com.example.bharathassignment2;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.concurrent.TimeUnit;

public class TaskNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_channel";
    private static final String SNOOZE_ACTION = "SNOOZE_ACTION";
    private static final String EDIT_ACTION = "EDIT_ACTION";
    private static final String DELETE_ACTION = "DELETE_ACTION";
    private static final String PREF_SNOOZE_COUNT = "snooze_count_";
    private static final int MAX_SNOOZE_COUNT = 5;
    private static final long SNOOZE_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(5); // 5 minutes snooze
    private static final int NOTIFICATION_ID_BASE = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        String taskId = intent.getStringExtra("taskId");
        String taskName = intent.getStringExtra("taskName");

        if (taskId == null) return;

        switch (action) {
            case SNOOZE_ACTION:
                handleSnooze(context, taskId, taskName);
                break;
            case EDIT_ACTION:
                handleEdit(context, taskId);
                break;
            case DELETE_ACTION:
                handleDelete(context, taskId);
                break;
            default:
                showTaskNotification(context, taskId, taskName);
        }
    }

    private void showTaskNotification(Context context, String taskId, String taskName) {
        createNotificationChannel(context);

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.putExtra("taskId", taskId);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                getRequestCode(taskId, 0),
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Action snoozeAction = createAction(context, taskId, taskName, SNOOZE_ACTION,
                R.drawable.img_1, "Snooze (5 min)", 1);

        NotificationCompat.Action editAction = createAction(context, taskId, taskName, EDIT_ACTION,
                R.drawable.img, "Edit", 2);

        NotificationCompat.Action deleteAction = createAction(context, taskId, taskName, DELETE_ACTION,
                R.drawable.img_2, "Delete", 3);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Reminder: " + taskName)
                .setContentText("Your scheduled task is due!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(contentIntent)
                .addAction(snoozeAction)
                .addAction(editAction)
                .addAction(deleteAction)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(getNotificationId(taskId), notification);
    }

    private NotificationCompat.Action createAction(Context context, String taskId,
                                                   String taskName, String action, int iconRes,
                                                   String title, int requestCodeModifier) {
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.setAction(action);
        intent.putExtra("taskId", taskId);
        intent.putExtra("taskName", taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                getRequestCode(taskId, requestCodeModifier),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Action.Builder(iconRes, title, pendingIntent).build();
    }

    private void handleSnooze(Context context, String taskId, String taskName) {
        SharedPreferences prefs = context.getSharedPreferences("Tasks", Context.MODE_PRIVATE);
        int snoozeCount = prefs.getInt(PREF_SNOOZE_COUNT + taskId, 0);

        if (snoozeCount >= MAX_SNOOZE_COUNT) {
            cancelNotification(context, taskId);
            return;
        }

        prefs.edit().putInt(PREF_SNOOZE_COUNT + taskId, snoozeCount + 1).apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("taskName", taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, getRequestCode(taskId, 0), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + SNOOZE_DELAY_MILLIS;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private void handleEdit(Context context, String taskId) {
        Intent intent = new Intent(context, EditTaskActivity.class);
        intent.putExtra("taskId", taskId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void handleDelete(Context context, String taskId) {
        SharedPreferences prefs = context.getSharedPreferences("Tasks", Context.MODE_PRIVATE);
        prefs.edit().remove(taskId).remove(PREF_SNOOZE_COUNT + taskId).apply();

        cancelNotification(context, taskId);
    }

    private void cancelNotification(Context context, String taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, TaskNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, getRequestCode(taskId, 0), intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
        NotificationManagerCompat.from(context).cancel(getNotificationId(taskId));
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for your tasks.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private static int getNotificationId(String taskId) {
        return NOTIFICATION_ID_BASE + Math.abs(taskId.hashCode() % 10000);
    }

    private int getRequestCode(String taskId, int modifier) {
        return Math.abs(taskId.hashCode()) + modifier;
    }

    public static void sendTaskReminder(Context context, String taskId, String taskName) {
        createNotificationChannel(context);

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.putExtra("taskId", taskId);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                taskId.hashCode(),
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Upcoming Task: " + taskName)
                .setContentText("This task is due within the next hour!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(getNotificationId(taskId), notification);
    }

}