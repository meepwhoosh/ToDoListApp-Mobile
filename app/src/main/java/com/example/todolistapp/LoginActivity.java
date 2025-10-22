package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

// Import untuk View Binding
import com.example.todolistapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    // Deklarasikan variabel binding
    private ActivityLoginBinding binding;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout menggunakan View Binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = new DatabaseHelper(this);

        binding.btnLogin.setOnClickListener(v -> {
            String user = binding.etUsername.getText().toString().trim();
            String pass = binding.etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Isi semua kolom!", Toast.LENGTH_SHORT).show();
            } else {
                boolean checkUser = db.checkUser(user, pass);
                if (checkUser) {
                    // Ambil User ID setelah login berhasil
                    int userId = db.getUserId(user);

                    // Simpan username dan USER ID ke SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", user);
                    editor.putInt("user_id", userId);
                    editor.apply();

                    Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Username atau password salah!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
