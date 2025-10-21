package com.example.todolistapp;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEditClick(HashMap<String, String> item);
        void onDeleteClick(HashMap<String, String> item);
        void onCheckedChange(HashMap<String, String> item, boolean isChecked);
    }

    private ArrayList<HashMap<String, String>> todoList;
    private final OnItemClickListener listener;

    public ToDoAdapter(ArrayList<HashMap<String, String>> todoList, OnItemClickListener listener) {
        this.todoList = todoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        HashMap<String, String> item = todoList.get(position);

        String title = item.get("title");
        String deadline = item.get("deadline");
        String completed = item.get("completed");

        h.txtTitle.setText(title != null ? title : "(tidak ada judul)");
        h.txtDeadline.setText(deadline != null && !deadline.isEmpty()
                ? "Deadline: " + deadline
                : "");

        boolean isChecked = completed != null && completed.equals("1");

        h.checkTodo.setOnCheckedChangeListener(null);
        h.checkTodo.setChecked(isChecked);
        h.txtTitle.setPaintFlags(isChecked ?
                h.txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                h.txtTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

        h.checkTodo.setOnCheckedChangeListener((b, c) ->
                listener.onCheckedChange(item, c));

        h.btnEdit.setOnClickListener(v -> listener.onEditClick(item));
        h.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return todoList != null ? todoList.size() : 0;
    }

    public void updateList(ArrayList<HashMap<String, String>> newList) {
        this.todoList = newList;
        notifyDataSetChanged();
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
