package com.example.bharathassignment2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import android.util.Log;
import java.util.ArrayList;
import java.util.Map;

public class ListTasksActivity extends AppCompatActivity {

    private WearableRecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_tasks);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Fixed: Pass "this" (context) to TaskAdapter
        taskAdapter = new TaskAdapter(this, taskList);
        recyclerView.setAdapter(taskAdapter);

        loadTasks();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("taskAdded", false) || intent.getBooleanExtra("taskUpdated", false)) {
            Log.d("TaskLoad", "Refreshing after task update");
            loadTasks();
        }
    }

    private void loadTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences("Tasks", MODE_PRIVATE);
        Map<String, ?> tasks = sharedPreferences.getAll();
        taskList.clear();

        Log.d("TaskLoad", "Total tasks found: " + tasks.size());

        for (Map.Entry<String, ?> entry : tasks.entrySet()) {
            try {
                String[] taskData = entry.getValue().toString().split(";");
                String taskName = taskData[0];
                String dueDate = (taskData.length > 1) ? taskData[1] : "No Due Date";

                taskList.add(new Task(entry.getKey(), taskName, dueDate));
                Log.d("TaskLoad", "Loaded Task - ID: " + entry.getKey() + ", Name: " + taskName);
            } catch (Exception e) {
                Log.e("TaskLoad", "Error parsing task: " + entry.getValue(), e);
            }
        }

        if (taskList.isEmpty()) {
            Log.d("TaskLoad", "No tasks retrieved!");
        }

        // ✅ Ensure adapter is properly updated
        taskAdapter.notifyDataSetChanged();
    }
}
