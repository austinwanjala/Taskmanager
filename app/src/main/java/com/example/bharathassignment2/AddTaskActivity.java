package com.example.bharathassignment2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddTaskActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private EditText etTaskId, etTaskName, etDueDateTime;
    private Button btnSaveTask, btnVoiceInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTaskId = findViewById(R.id.etTaskId);
        etTaskName = findViewById(R.id.etTaskName);
        etDueDateTime = findViewById(R.id.etDueDate);  // Now includes time
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnVoiceInput = findViewById(R.id.btnVoiceInput);

        // Request microphone permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        btnVoiceInput.setOnClickListener(v -> startVoiceRecognition());

        // Open Date-Time Picker when clicking Due Date-Time field
        etDueDateTime.setOnClickListener(v -> showDateTimePicker());

        btnSaveTask.setOnClickListener(v -> {
            String taskId = etTaskId.getText().toString().trim();
            String taskName = etTaskName.getText().toString().trim();
            String dueDateTime = etDueDateTime.getText().toString().trim();

            if (taskId.isEmpty() || taskName.isEmpty() || dueDateTime.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                saveTask(taskId, taskName, dueDateTime);
                Toast.makeText(AddTaskActivity.this, "Task Saved", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(AddTaskActivity.this, ListTasksActivity.class);
                intent.putExtra("taskAdded", true);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;

                    // After selecting a date, open Time Picker
                    showTimePicker(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker(String selectedDate) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Show Time Picker
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    etDueDateTime.setText(selectedDate + " " + selectedTime);
                },
                hour, minute, true // 24-hour format
        );
        timePickerDialog.show();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Your device does not support voice input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                etTaskName.setText(result.get(0)); // Set the recognized text
            }
        }
    }

    private void saveTask(String taskId, String taskName, String dueDateTime) {
        getSharedPreferences("Tasks", MODE_PRIVATE)
                .edit()
                .putString(taskId, taskName + ";" + dueDateTime)
                .apply();

        Log.d("TaskSave", "Saved Task - ID: " + taskId + ", Name: " + taskName + ", Due DateTime: " + dueDateTime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission is required for voice input", Toast.LENGTH_LONG).show();
            }
        }
    }
}
