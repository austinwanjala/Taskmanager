package com.example.bharathassignment2;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTaskActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";

    private EditText etTaskName, etDueDateTime;
    private Button btnSave, btnPickDateTime;
    private String taskId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        createNotificationChannel();

        prefs = getSharedPreferences("Tasks", MODE_PRIVATE);
        initViews();
        loadTaskData();
        setupListeners();
    }

    private void initViews() {
        etTaskName = findViewById(R.id.etTaskName);
        etDueDateTime = findViewById(R.id.etDueDateTime);
        btnSave = findViewById(R.id.btnSave);
        btnPickDateTime = findViewById(R.id.btnPickDateTime);
    }

    private void loadTaskData() {
        Intent intent = getIntent();
        taskId = intent.getStringExtra("taskId");
        String taskName = intent.getStringExtra("taskName");
        String dueDateTime = intent.getStringExtra("dueDateTime");

        if (taskId == null || taskName == null || dueDateTime == null) {
            showErrorAndFinish("Error: Missing task data");
            return;
        }

        etTaskName.setText(taskName);
        etDueDateTime.setText(dueDateTime);
        etTaskName.requestFocus();
        showSoftKeyboard(etTaskName);
    }

    private void setupListeners() {
        btnPickDateTime.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    showTimePicker(calendar); // Show time picker after date selection
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Restrict past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    // Ensure only future times are selected
                    if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
                        etDueDateTime.setText(sdf.format(calendar.getTime()));
                    } else {
                        Toast.makeText(this, "Please select a future time!", Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void saveChanges() {
        String newName = etTaskName.getText().toString().trim();
        String newDateTime = etDueDateTime.getText().toString().trim();

        if (!validateInput(newName, newDateTime)) {
            return;
        }

        if (saveTaskToPreferences(taskId, newName, newDateTime)) {
            if (scheduleNotification(taskId, newName, newDateTime)) {
                Toast.makeText(this, "Task updated successfully!", Toast.LENGTH_SHORT).show();

                // ✅ Send RESULT_OK so the main activity reloads data
                Intent resultIntent = new Intent();
                resultIntent.putExtra("taskUpdated", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        } else {
            Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean validateInput(String name, String dateTime) {
        if (name.isEmpty()) {
            etTaskName.setError("Task name cannot be empty");
            return false;
        }
        if (dateTime.isEmpty()) {
            etDueDateTime.setError("Due date/time cannot be empty");
            return false;
        }

        // Validate date format
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            sdf.parse(dateTime);
        } catch (ParseException e) {
            etDueDateTime.setError("Use format: DD/MM/YYYY HH:MM");
            return false;
        }

        return true;
    }
    private void deleteTask() {
        prefs.edit().remove(taskId).apply();
        cancelNotification(taskId);
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
    private void cancelNotification(String taskId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent notificationIntent = new Intent(this, TaskNotificationReceiver.class);
        int requestCode = taskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("Notification", "Notification cancelled for task: " + taskId);
    }

    private boolean saveTaskToPreferences(String id, String name, String dateTime) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(id, name + ";" + dateTime);
            boolean success = editor.commit(); // ✅ Use commit() instead of apply() to ensure immediate save
            Log.d("TaskSave", "Task saved: " + id + " -> " + name + ";" + dateTime);
            return success;
        } catch (Exception e) {
            Log.e("TaskSave", "Error saving task", e);
            return false;
        }
    }

    private boolean scheduleNotification(String taskId, String taskName, String dueDateTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date dueDate = sdf.parse(dueDateTime);

            if (dueDate == null || !dueDate.after(new Date())) {
                Toast.makeText(this, "Please select a future date/time", Toast.LENGTH_SHORT).show();
                return false;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager == null) {
                return false;
            }

            // Check exact alarm permission for Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                requestExactAlarmPermission();
                return false;
            }

            Intent notificationIntent = new Intent(this, TaskNotificationReceiver.class);
            notificationIntent.putExtra("taskId", taskId);
            notificationIntent.putExtra("taskName", taskName);

            int requestCode = taskId.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            dueDate.getTime(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            dueDate.getTime(),
                            pendingIntent
                    );
                }
                Log.d("Notification", "Notification scheduled for: " + dueDate);
                return true;
            } catch (SecurityException e) {
                Log.e("Notification", "Exact alarm permission denied", e);
                requestExactAlarmPermission();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Toast.makeText(this,
                    "Exact alarm permission required for reminders",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for task reminder notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void showSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
