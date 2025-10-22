package com.example.todolistapp;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * File: ToDoAdapter.java
 * Tugas Dede - Direvisi untuk menggunakan ToDoModel dan ToDoFragment.
 */
public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private Context context;
    private List<ToDoModel> todoList;
    private ToDoFragment fragment; // Referensi ke fragment untuk callback

    public ToDoAdapter(Context context, List<ToDoModel> todoList, ToDoFragment fragment) {
        this.context = context;
        this.todoList = todoList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToDoModel item = todoList.get(position);

        holder.txtTitle.setText(item.getTaskName());

        // Logika untuk menampilkan deadline
        if (item.getDueDate() != null && !item.getDueDate().isEmpty()) {
            holder.txtDeadline.setText("Deadline: " + item.getDueDate());
            holder.txtDeadline.setVisibility(View.VISIBLE);
        } else {
            holder.txtDeadline.setVisibility(View.GONE);
        }

        // Logika untuk status (checkbox dan coretan)
        boolean isCompleted = item.getStatus() == 1;
        holder.checkTodo.setOnCheckedChangeListener(null); // Hindari trigger listener saat re-bind
        holder.checkTodo.setChecked(isCompleted);

        if (isCompleted) {
            holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtTitle.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        } else {
            holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtTitle.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        // Set listener untuk checkbox
        holder.checkTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newStatus = isChecked ? 1 : 0;
            fragment.updateTaskStatus(item.getId(), newStatus);
        });

        // (DIUPDATE) Set listener untuk tombol EDIT
        holder.btnEdit.setOnClickListener(v -> {
            fragment.showAddOrEditTodoDialog(item);
        });

        // Set listener untuk tombol DELETE
        holder.btnDelete.setOnClickListener(v -> {
            fragment.deleteTask(item.getId());
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkTodo;
        TextView txtTitle, txtDeadline;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTodo = itemView.findViewById(R.id.checkTodo);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDeadline = itemView.findViewById(R.id.txtDeadline);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
