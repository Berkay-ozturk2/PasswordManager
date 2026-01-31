package com.example.passwordmanager;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ayarlar");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        if (findViewById(R.id.btnClearAll) != null) {
            findViewById(R.id.btnClearAll).setOnClickListener(v -> showClearDataDialog());
        }
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tüm Verileri Sil")
                .setMessage("Tüm kayıtlar silinecektir. Onaylıyor musunuz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    new Thread(() -> {
                        db.accountDao().deleteAll();
                        runOnUiThread(() -> Toast.makeText(this, "Temizlendi", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}