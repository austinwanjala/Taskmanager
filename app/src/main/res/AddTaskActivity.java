package com.example.bharathassignment2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_VOICE_INPUT = 100;
    private EditText etTaskId, etTaskName;
    private Button btnSaveTask, btnVoiceInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTaskId = findViewById(R.id.etTaskId);
        etTaskName = findViewById(R.id.etTaskName);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnVoiceInput = findViewById(R.id.btnVoiceInput);

        btnSaveTask.setOnClickListener(v -> saveTask());
        
        btnVoiceInput.setOnClickListener(v -> startVoiceInput());
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your task name");

        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
            Log.e("VoiceInput", "Error starting voice input", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_VOICE_INPUT && resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                etTaskName.setText(spokenText);
                
                // Auto-generate a simple ID if empty
                if (etTaskId.getText().toString().trim().isEmpty()) {
                    etTaskId.setText("T-" + System.currentTimeMillis() % 10000);
                }
            }
        }
    }

    private void saveTask() {
        String taskId = etTaskId.getText().toString().trim();
        String taskName = etTaskName.getText().toString().trim();

        if (taskId.isEmpty() || taskName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Tasks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(taskId, taskName + ";No Due Date");
        editor.apply();

        Log.d("TaskSave", "Saved Task - ID: " + taskId + ", Name: " + taskName);

        // Return to list with refresh flag
        Intent intent = new Intent(this, ListTasksActivity.class);
        intent.putExtra("taskAdded", true);
        startActivity(intent);
        finish();
    }
}