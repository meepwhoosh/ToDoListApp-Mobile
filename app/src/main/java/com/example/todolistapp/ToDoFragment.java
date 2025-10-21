package com.example.todolistapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ToDoFragment extends Fragment {

    public ToDoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        // Ambil komponen dari layout
        TextView tvHelloUser = view.findViewById(R.id.tv_hello_user);
        TextView tvCurrentDate = view.findViewById(R.id.tv_current_date);
        Spinner spinnerFilter = view.findViewById(R.id.spinner_filter);

        // 1. Ambil SharedPreferences dan tampilkan nama pengguna
        // Ganti "user_prefs" dan "username" dengan key yang Anda gunakan saat menyimpan
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User"); // "User" sebagai default
        tvHelloUser.setText("Hello, " + username);

        // 2. Tampilkan tanggal saat ini
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", new Locale("id", "ID"));
            String formattedDate = today.format(formatter);
            tvCurrentDate.setText(formattedDate);
        }

        // 3. Setup Spinner untuk filter
        String[] filterOptions = {"Ongoing", "Missed", "Completed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        // Untuk FAB dan RecyclerView, Anda akan menambahkan logika lebih lanjut di sini,
        // seperti OnClickListener untuk FAB yang membuka dialog tambah tugas.

        return view;
    }
}

