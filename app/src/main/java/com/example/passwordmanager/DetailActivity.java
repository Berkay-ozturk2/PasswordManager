package com.example.passwordmanager;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    // Şifrenin görünürlük durumunu tutan değişken
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ÖNEMLİ: Eğer tasarımın activity_detail2.xml içindeyse burası doğru
        setContentView(R.layout.activity_detail2);

        // 1. Paketleri Intent'ten alıyoruz
        String site = getIntent().getStringExtra("siteName");
        String user = getIntent().getStringExtra("username");
        String pass = getIntent().getStringExtra("password");

        // 2. Tasarımdaki (XML) bileşenleri Java'ya bağlıyoruz
        TextView tvSite = findViewById(R.id.detailSiteName);
        TextView tvUser = findViewById(R.id.detailUsername);
        EditText etPass = findViewById(R.id.detailPassword);
        Button btnToggle = findViewById(R.id.btnTogglePassword);

        // 3. Verileri yerleştiriyoruz
        tvSite.setText(site);
        tvUser.setText(user);
        etPass.setText(pass);

        // 4. Şifreyi Göster/Gizle butonu mantığı
        btnToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Şifreyi gizle (noktalı yap)
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggle.setText("Şifreyi Göster");
            } else {
                // Şifreyi görünür yap
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggle.setText("Şifreyi Gizle");
            }
            isPasswordVisible = !isPasswordVisible;

            // İmleci (cursor) metnin sonuna taşı ki kayma olmasın
            etPass.setSelection(etPass.getText().length());
        });
    }
}