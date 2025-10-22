package com.example.todolistapp;

// Import yang diperlukan
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

// Import untuk View Binding
import com.example.todolistapp.databinding.FragmentTodoBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * File: ToDoFragment.java
 * Menggabungkan setup (User, Date, Filter) dengan Logika CRUD.
 */
public class ToDoFragment extends Fragment {

    // Variabel binding untuk mengakses semua view di layout
    private FragmentTodoBinding binding;

    // == BAGIAN DEDE (RecyclerView) ==
    private ToDoAdapter adapter;
    private List<ToDoModel> todoList;

    // == BAGIAN HAFIZ (Database & Dialog) ==
    private DatabaseHelper dbHelper;
    private int currentUserId; // ID user yang sedang login

    // Variabel untuk menyimpan pilihan tanggal & waktu dari dialog
    private final Calendar selectedDateTime = Calendar.getInstance();
    private String selectedDateString = null;
    private String selectedTimeString = null;

    public ToDoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout (Tugas Sofia) - menggunakan View Binding
        binding = FragmentTodoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * onViewCreated dipanggil setelah onCreateView.
     * SEMUA LOGIKA (setup UI, listener, load data) kita letakkan di sini.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null || getActivity() == null) return;

        // =================================================================
        // == BAGIAN SETUP (User, Tanggal, Filter) ==
        // =================================================================

        // 1. Ambil SharedPreferences dan tampilkan nama pengguna
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        currentUserId = sharedPreferences.getInt("user_id", -1);
        binding.tvHelloUser.setText("Hello, " + username + "!");

        // 2. Tampilkan tanggal saat ini
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", new Locale("id", "ID"));
            binding.tvCurrentDate.setText(today.format(formatter));
        }

        // 3. Setup Spinner untuk filter (Tugas Dede)
        String[] filterOptions = {"Ongoing", "Missed", "Completed"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(spinnerAdapter);
        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadTodos(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // =================================================================
        // == BAGIAN HAFIZ (CRUD) & DEDE (RecyclerView) ==
        // =================================================================

        // 4. Inisialisasi Database Helper
        dbHelper = new DatabaseHelper(getContext());
        todoList = new ArrayList<>();

        // 5. Inisialisasi RecyclerView (Tugas Dede)
        adapter = new ToDoAdapter(getContext(), todoList, this);
        binding.recyclerViewTodo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTodo.setAdapter(adapter);

        // 6. Inisialisasi Tombol Tambah (FAB) (Tugas Hafiz)
        binding.fabAddTodo.setOnClickListener(v -> showAddOrEditTodoDialog(null));

        // 7. Muat data pertama kali (Tugas Hafiz)
        loadTodos("Ongoing");
    }

    // =====================================================================
    // == BAGIAN HAFIZ (Fungsi CRUD) - SESUAI REVISI TERAKHIR ==
    // =====================================================================

    /**
     * CREATE / UPDATE: Menampilkan dialog untuk menambah atau mengedit ToDo.
     * @param todoToEdit Model ToDo yang akan diedit. Jika null, maka mode "Add".
     */
    public void showAddOrEditTodoDialog(@Nullable final ToDoModel todoToEdit) {
        if (getContext() == null || getActivity() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_todo, null);
        builder.setView(dialogView);

        EditText etTaskName = dialogView.findViewById(R.id.etTaskName);
        Spinner spCustomLabel = dialogView.findViewById(R.id.spCustomLabel);
        EditText etTaskDesc = dialogView.findViewById(R.id.etTaskDescription);
        SwitchCompat switchDeadline = dialogView.findViewById(R.id.switchDeadline);
        LinearLayout layoutPickers = dialogView.findViewById(R.id.layoutDeadlinePickers);
        TextView tvSelectDate = dialogView.findViewById(R.id.tvSelectDate);
        TextView tvSelectTime = dialogView.findViewById(R.id.tvSelectTime);
        Button btnAction = dialogView.findViewById(R.id.btnCreateTodo);

        selectedDateString = null;
        selectedTimeString = null;

        final boolean isEditMode = todoToEdit != null;
        if (isEditMode) {
            btnAction.setText("Update");
            etTaskName.setText(todoToEdit.getTaskName());
            etTaskDesc.setText(todoToEdit.getTaskDescription());

            if (todoToEdit.getDueDate() != null && !todoToEdit.getDueDate().isEmpty()) {
                switchDeadline.setChecked(true);
                layoutPickers.setVisibility(View.VISIBLE);
                selectedDateString = todoToEdit.getDueDate();
                selectedTimeString = todoToEdit.getDueTime();
                tvSelectDate.setText(selectedDateString);
                tvSelectTime.setText(selectedTimeString != null ? selectedTimeString : "Select Time");
            }
        } else {
            btnAction.setText("Create");
        }

        switchDeadline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutPickers.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                selectedDateString = null;
                selectedTimeString = null;
                tvSelectDate.setText("Select Date");
                tvSelectTime.setText("Select Time");
            }
        });

        tvSelectDate.setOnClickListener(v -> showDatePicker(tvSelectDate));
        tvSelectTime.setOnClickListener(v -> showTimePicker(tvSelectTime));

        AlertDialog dialog = builder.create();

        btnAction.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString().trim();
            if (taskName.isEmpty()) {
                etTaskName.setError("Nama tugas tidak boleh kosong!");
                return;
            }

            String description = etTaskDesc.getText().toString().trim();
            String label = spCustomLabel.getSelectedItem().toString();

            boolean isSuccess;
            if (isEditMode) {
                isSuccess = dbHelper.updateTodoDetails(todoToEdit.getId(), taskName, description, selectedDateString, selectedTimeString, label);
                Toast.makeText(getContext(), isSuccess ? "Tugas diperbarui" : "Gagal memperbarui tugas", Toast.LENGTH_SHORT).show();
            } else {
                isSuccess = dbHelper.addTodo(taskName, description, selectedDateString, selectedTimeString, label, currentUserId);
                Toast.makeText(getContext(), isSuccess ? "Tugas ditambahkan" : "Gagal menambahkan tugas", Toast.LENGTH_SHORT).show();
            }

            if (isSuccess) {
                loadTodos(binding.spinnerFilter.getSelectedItem().toString());
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Helper untuk menampilkan DatePicker
     */
    private void showDatePicker(TextView tvSelectDate) {
        if(getContext() == null) return;
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedDateString = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
            tvSelectDate.setText(selectedDateString);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Helper untuk menampilkan TimePicker
     */
    private void showTimePicker(TextView tvSelectTime) {
        if(getContext() == null) return;
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            selectedTimeString = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            tvSelectTime.setText(selectedTimeString);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    /**
     * READ: Memuat (load) semua data ToDo dari database ke RecyclerView.
     */
    private void loadTodos(String filter) {
        if (adapter == null || dbHelper == null || binding == null) return;

        todoList.clear();
        Cursor cursor = dbHelper.getTodosFiltered(currentUserId, filter);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TODO_ID));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_NAME));
                String taskDesc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DESC));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DUE_DATE));
                String dueTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DUE_TIME));
                String label = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LABEL));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS));

                todoList.add(new ToDoModel(id, taskName, taskDesc, dueDate, dueTime, label, status, currentUserId));
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (todoList.isEmpty()) {
            binding.recyclerViewTodo.setVisibility(View.GONE);
            binding.layoutNoTask.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewTodo.setVisibility(View.VISIBLE);
            binding.layoutNoTask.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * UPDATE: Fungsi ini akan dipanggil dari ToDoAdapter untuk mengubah status (centang).
     */
    public void updateTaskStatus(int todoId, int newStatus) {
        if(binding == null) return;
        boolean isSuccess = dbHelper.updateTodoStatus(todoId, newStatus);
        Toast.makeText(getContext(), isSuccess ? "Status tugas diperbarui" : "Gagal memperbarui status", Toast.LENGTH_SHORT).show();
        if (isSuccess) {
            loadTodos(binding.spinnerFilter.getSelectedItem().toString());
        }
    }

    /**
     * DELETE: Fungsi ini akan dipanggil dari ToDoAdapter.
     */
    public void deleteTask(int todoId) {
        if(getContext() == null || binding == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Hapus Tugas")
                .setMessage("Apakah Anda yakin ingin menghapus tugas ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    boolean isSuccess = dbHelper.deleteTodo(todoId);
                    Toast.makeText(getContext(), isSuccess ? "Tugas dihapus" : "Gagal menghapus tugas", Toast.LENGTH_SHORT).show();
                    if (isSuccess) {
                        loadTodos(binding.spinnerFilter.getSelectedItem().toString());
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    /**
     * Selalu bersihkan binding saat view dihancurkan
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Selalu tutup database saat fragment hancur
     */
    @Override
    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
