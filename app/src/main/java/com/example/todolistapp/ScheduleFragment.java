package com.example.todolistapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Import untuk View Binding
import com.example.todolistapp.databinding.FragmentScheduleBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    // Deklarasikan variabel binding
    private FragmentScheduleBinding binding;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout menggunakan View Binding
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null || getActivity() == null) return;

        // Ambil SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");

        // Gunakan binding untuk mengakses view
        binding.tvHelloUserSchedule.setText(getString(R.string.hello_user, username));

        // Tampilkan tanggal saat ini
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", new Locale("id", "ID"));
            binding.tvCurrentDateSchedule.setText(today.format(formatter));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Set binding ke null untuk menghindari memory leak
        binding = null;
    }
}
