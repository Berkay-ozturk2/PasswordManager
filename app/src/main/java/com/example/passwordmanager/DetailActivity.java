package com.example.passwordmanager; // Bu satır senin paket adınla aynı olmalı

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tasarım dosyanın adı activity_detail2 ise burası doğru
        setContentView(R.layout.activity_detail2);

        // 1. Intent ile gelen verileri alıyoruz
        String site = getIntent().getStringExtra("siteName");
        String user = getIntent().getStringExtra("username");
        String pass = getIntent().getStringExtra("password");

        // 2. XML'deki bileşenleri Java'ya bağlıyoruz
        TextView tvSite = findViewById(R.id.detailSiteName);
        TextView tvUser = findViewById(R.id.detailUsername);
        EditText etPass = findViewById(R.id.detailPassword);
        Button btnToggle = findViewById(R.id.btnTogglePassword);

        // 3. Verileri ekran yerleştiriyoruz
        tvSite.setText(site);
        tvUser.setText(user);
        etPass.setText(pass);

        // 4. Şifreyi Göster/Gizle butonu
        btnToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Şifreyi gizle
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggle.setText("Şifreyi Göster");
            } else {
                // Şifreyi görünür yap
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggle.setText("Şifreyi Gizle");
            }
            isPasswordVisible = !isPasswordVisible;
            // İmleci sona taşı
            etPass.setSelection(etPass.getText().length());
        });
    }
}