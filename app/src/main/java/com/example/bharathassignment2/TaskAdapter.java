package com.example.bharathassignment2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> taskList;
    private Context context;

    public TaskAdapter(Context context, ArrayList<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTaskId.setText(task.getTaskId());
        holder.tvTaskName.setText(task.getTaskName());
        holder.tvDueDateTime.setText(task.getDueDateTime());

        // Edit Button Click Listener
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTaskActivity.class);
            intent.putExtra("taskId", task.getTaskId());
            intent.putExtra("taskName", task.getTaskName());
            intent.putExtra("dueDateTime", task.getDueDateTime());

            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("TaskAdapter", "Failed to start EditTaskActivity", e);
            }
        });

        // Delete Button Click Listener
        holder.btnDelete.setOnClickListener(v -> deleteTask(task, position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void deleteTask(Task task, int position) {
        // Remove from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("Tasks", Context.MODE_PRIVATE);
        prefs.edit().remove(task.getTaskId()).apply();

        // Cancel scheduled notification
        cancelNotification(task.getTaskId());

        // Remove from list and notify RecyclerView
        taskList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, taskList.size());

        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
    }

    private void cancelNotification(String taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent notificationIntent = new Intent(context, TaskNotificationReceiver.class);
        int requestCode = taskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d("Notification", "Notification cancelled for task: " + taskId);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskId, tvTaskName, tvDueDateTime;
        Button btnEdit, btnDelete; // Added Delete Button

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskId = itemView.findViewById(R.id.tvTaskId);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvDueDateTime = itemView.findViewById(R.id.tvDueDateTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Added Delete Button
        }
    }
}
