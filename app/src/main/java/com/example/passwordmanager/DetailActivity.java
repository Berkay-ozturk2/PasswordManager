package com.example.passwordmanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private EditText etTitle, etUser, etPass;
    private int accountId;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        accountId = getIntent().getIntExtra("id", -1);
        category = getIntent().getStringExtra("category");

        etTitle = findViewById(R.id.etDetailTitle);
        etUser = findViewById(R.id.etDetailUsername);
        etPass = findViewById(R.id.etDetailPassword);

        etTitle.setText(getIntent().getStringExtra("title"));
        etUser.setText(getIntent().getStringExtra("username"));
        etPass.setText(getIntent().getStringExtra("password"));

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            String t = etTitle.getText().toString().trim();
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            if (!t.isEmpty() && !u.isEmpty() && !p.isEmpty()) {
                new Thread(() -> {
                    Account updated = new Account(t, u, p, category);
                    updated.id = accountId;
                    AppDatabase.getInstance(this).accountDao().update(updated);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Güncellendi", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Sil").setMessage("Emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(this).accountDao().deleteById(accountId);
                            runOnUiThread(() -> { finish(); });
                        }).start();
                    }).setNegativeButton("Hayır", null).show();
        });
    }
}