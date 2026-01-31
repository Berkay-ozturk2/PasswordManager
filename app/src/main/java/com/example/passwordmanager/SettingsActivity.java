package com.example.passwordmanager; // Paket ismi mutlaka bu olmalı

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

        // Geri butonu için ActionBar ayarı
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ayarlar");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        // activity_settings.xml içindeki buton ID'si btnClearAll olmalı
        if (findViewById(R.id.btnClearAll) != null) {
            findViewById(R.id.btnClearAll).setOnClickListener(v -> showClearDataDialog());
        }
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Verileri Sil")
                .setMessage("Tüm şifreleriniz silinecek. Emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    new Thread(() -> {
                        db.accountDao().deleteAll(); // Dao'ya bu metodu ekleyeceğiz
                        runOnUiThread(() -> Toast.makeText(this, "Tüm veriler silindi", Toast.LENGTH_SHORT).show());
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