package com.example.passwordmanager;

import android.os.Bundle;
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

        // Intent'ten verileri güvenli bir şekilde alıyoruz
        accountId = getIntent().getIntExtra("id", -1);
        category = getIntent().getStringExtra("category");

        etTitle = findViewById(R.id.etDetailTitle);
        etUser = findViewById(R.id.etDetailUsername);
        etPass = findViewById(R.id.etDetailPassword);

        etTitle.setText(getIntent().getStringExtra("title"));
        etUser.setText(getIntent().getStringExtra("username"));
        etPass.setText(getIntent().getStringExtra("password"));

        // GÜNCELLEME BUTONU
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
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            }
        });

        // SİLME BUTONU
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sil")
                    .setMessage("Bu hesabı silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            // Artık AccountDao'da bu metot tanımlı olduğu için hata vermeyecektir
                            AppDatabase.getInstance(this).accountDao().deleteById(accountId);
                            runOnUiThread(this::finish);
                        }).start();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
        });
    }
}