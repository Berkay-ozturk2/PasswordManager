package com.example.passwordmanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        int id = getIntent().getIntExtra("id", -1);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvUser = findViewById(R.id.tvDetailUsername);
        TextView tvPass = findViewById(R.id.tvDetailPassword);
        Button btnDelete = findViewById(R.id.btnDelete);

        if (getIntent() != null) {
            tvTitle.setText(getIntent().getStringExtra("title"));
            tvUser.setText(getIntent().getStringExtra("username"));
            tvPass.setText(getIntent().getStringExtra("password"));
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Hesabı Sil")
                        .setMessage("Bu hesabı silmek istediğinize emin misiniz?")
                        .setPositiveButton("Evet", (dialog, which) -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(this).accountDao().deleteById(id);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Hesap silindi", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }).start();
                        })
                        .setNegativeButton("Hayır", null).show();
            });
        }
    }
}