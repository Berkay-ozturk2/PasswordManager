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
    private CryptoHelper cryptoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // GÜVENLİ EKRAN: Ekran görüntüsü engelleme
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_detail);

        cryptoHelper = new CryptoHelper();
        accountId = getIntent().getIntExtra("id", -1);
        category = getIntent().getStringExtra("category");

        etTitle = findViewById(R.id.etDetailTitle);
        etUser = findViewById(R.id.etDetailUsername);
        etPass = findViewById(R.id.etDetailPassword);
        ImageButton btnTogglePass = findViewById(R.id.btnTogglePass);

        // Verileri Doldurma
        etTitle.setText(getIntent().getStringExtra("title"));
        etUser.setText(getIntent().getStringExtra("username"));

        // Şifreyi çözerek gösteriyoruz
        String encryptedFromDb = getIntent().getStringExtra("password");
        etPass.setText(cryptoHelper.decrypt(encryptedFromDb));

        // Şifre Gizle/Göster Butonu
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

        // Pano Kopyalama İşlemleri (ClipboardHelper kullanımı)
        findViewById(R.id.btnCopyUser).setOnClickListener(v ->
                ClipboardHelper.copyToClipboard(this, etUser.getText().toString(), "Kullanıcı adı kopyalandı"));

        findViewById(R.id.btnCopyPass).setOnClickListener(v ->
                ClipboardHelper.copyToClipboard(this, etPass.getText().toString(), "Şifre kopyalandı"));

        // Güncelleme Butonu
        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            String t = etTitle.getText().toString().trim();
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();

            if (!t.isEmpty() && !u.isEmpty() && !p.isEmpty()) {
                new Thread(() -> {
                    // Kaydederken CryptoHelper ile şifreleme zorunlu
                    String encryptedNewPass = cryptoHelper.encrypt(p);
                    Account updated = new Account(t, u, encryptedNewPass, category);
                    updated.id = accountId;
                    AppDatabase.getInstance(this).accountDao().update(updated);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Başarıyla güncellendi", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }).start();
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            }
        });

        // Silme Butonu
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Sil").setMessage("Emin misiniz?")
                    .setPositiveButton("Evet", (d, w) -> new Thread(() -> {
                        AppDatabase.getInstance(this).accountDao().deleteById(accountId);
                        runOnUiThread(this::finish);
                    }).start()).setNegativeButton("Hayır", null).show();
        });
    }
}