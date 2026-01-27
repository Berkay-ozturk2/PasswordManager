package com.example.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

        etTitle.setText(getIntent().getStringExtra("title"));
        etUser.setText(getIntent().getStringExtra("username"));

        String encryptedFromDb = getIntent().getStringExtra("password");
        etPass.setText(cryptoHelper.decrypt(encryptedFromDb));

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

        findViewById(R.id.btnCopyUser).setOnClickListener(v -> copyToClipboard(etUser.getText().toString(), "Kullanıcı adı kopyalandı"));
        findViewById(R.id.btnCopyPass).setOnClickListener(v -> copyToClipboard(etPass.getText().toString(), "Şifre kopyalandı"));

        findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            String t = etTitle.getText().toString().trim();
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            if (!t.isEmpty() && !u.isEmpty() && !p.isEmpty()) {
                new Thread(() -> {
                    String encryptedNewPass = cryptoHelper.encrypt(p);
                    Account updated = new Account(t, u, encryptedNewPass, category);
                    updated.id = accountId;
                    AppDatabase.getInstance(this).accountDao().update(updated);
                    runOnUiThread(() -> { Toast.makeText(this, "Başarıyla güncellendi", Toast.LENGTH_SHORT).show(); finish(); });
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

    private void copyToClipboard(String text, String message) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("PasswordManager", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, message + " (3 dk sonra silinecek)", Toast.LENGTH_SHORT).show();

            // 3 DAKİKA SONRA PANOYU TEMİZLE
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ClipData currentClip = clipboard.getPrimaryClip();
                if (currentClip != null && currentClip.getDescription().getLabel() != null &&
                        currentClip.getDescription().getLabel().equals("PasswordManager")) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
                    Toast.makeText(this, "Güvenlik için pano temizlendi", Toast.LENGTH_SHORT).show();
                }
            }, 180000);
        }
    }
}