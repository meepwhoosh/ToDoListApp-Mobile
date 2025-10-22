package com.example.todolistapp; // Pastikan package-nya benar

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    // Durasi splash screen dalam milidetik (misal: 2000 ms = 2 detik)
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Menggunakan Handler untuk menunda perpindahan ke activity berikutnya
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Buat Intent untuk memulai LoginActivity
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);

                // Tutup WelcomeActivity agar tidak bisa kembali dengan tombol "back"
                finish();
            }
        }, SPLASH_DELAY);
    }
}
