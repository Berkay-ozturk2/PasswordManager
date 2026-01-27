package com.example.passwordmanager;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private EditText etTitle, etUser, etPass;
    private int accountId;
    private String category;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_detail);

        accountId = getIntent().getIntExtra("id", -1);
        category = getIntent().getStringExtra("category");

        etTitle = findViewById(R.id.etDetailTitle);
        etUser = findViewById(R.id.etDetailUsername);
        etPass = findViewById(R.id.etDetailPassword);
        ImageButton btnTogglePass = findViewById(R.id.btnTogglePass);

        etTitle.setText(getIntent().getStringExtra("title"));
        etUser.setText(getIntent().getStringExtra("username"));

        // Singleton Decrypt
        String encryptedFromDb = getIntent().getStringExtra("password");
        etPass.setText(CryptoHelper.getInstance().decrypt(encryptedFromDb));

        btnTogglePass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePass.setImageResource(android.R.drawable.ic_menu_view);
                isPasswordVisible = false;
            } else {
                etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePass.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                isPasswordVisible = true;
            }
            etPass.setSelection(etPass.getText().length());
        });

        // Merkezi Kopyalama
        findViewById(R.id.btnCopyUser).setOnClickListener(v ->
                ClipboardHelper.copyToClipboard(this, etUser.getText().toString(), "Kullanıcı adı kopyalandı"));

        findViewById(R.id.btnCopyPass).setOnClickListener(v ->
                ClipboardHelper.copyToClipboard(this, etPass.getText().toString(), "Şifre kopyalandı"));

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            String t = etTitle.getText().toString().trim();
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            if (!t.isEmpty() && !u.isEmpty() && !p.isEmpty()) {
                new Thread(() -> {
                    String encryptedNewPass = CryptoHelper.getInstance().encrypt(p);
                    Account updated = new Account(t, u, encryptedNewPass, category);
                    updated.id = accountId;
                    AppDatabase.getInstance(this).accountDao().update(updated);
                    runOnUiThread(() -> { Toast.makeText(this, "Güncellendi", Toast.LENGTH_SHORT).show(); finish(); });
                }).start();
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Sil").setMessage("Emin misiniz?")
                    .setPositiveButton("Evet", (d, w) -> new Thread(() -> {
                        AppDatabase.getInstance(this).accountDao().deleteById(accountId);
                        runOnUiThread(this::finish);
                    }).start()).setNegativeButton("Hayır", null).show();
        });
    }
}