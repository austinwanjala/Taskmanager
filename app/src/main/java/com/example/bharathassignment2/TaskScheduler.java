package com.example.bharathassignment2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class TaskScheduler {
    private static final int HOURLY_CHECK_REQUEST_CODE = 5000;

    public static void scheduleHourlyTaskCheck(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, TaskCheckReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, HOURLY_CHECK_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis();
        long interval = AlarmManager.INTERVAL_HOUR;

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent
        );
    }
}
