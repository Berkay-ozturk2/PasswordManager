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

        // Üst bar başlığı ve geri butonu
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ayarlar");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        // XML'deki buton ID'si ile eşleşmeli
        if (findViewById(R.id.btnClearAll) != null) {
            findViewById(R.id.btnClearAll).setOnClickListener(v -> showClearDataDialog());
        }
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Dikkat")
                .setMessage("Tüm kayıtlı şifreleriniz silinecek. Emin misiniz?")
                .setPositiveButton("Evet, Sil", (dialog, which) -> {
                    new Thread(() -> {
                        db.accountDao().deleteAll(); // Az önce eklediğimiz metot
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Tüm veriler temizlendi", Toast.LENGTH_SHORT).show();
                        });
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