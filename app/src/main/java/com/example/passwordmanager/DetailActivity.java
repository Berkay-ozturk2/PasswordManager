package com.example.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class DetailActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private AppDatabase db;
    private int accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail2);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "sifre-kasasi")
                .allowMainThreadQueries().build();

        // Verileri al
        accountId = getIntent().getIntExtra("id", -1);
        String site = getIntent().getStringExtra("siteName");
        String user = getIntent().getStringExtra("username");
        String pass = getIntent().getStringExtra("password");

        TextView tvSite = findViewById(R.id.detailSiteName);
        TextView tvUser = findViewById(R.id.detailUsername);
        EditText etPass = findViewById(R.id.detailPassword);
        Button btnToggle = findViewById(R.id.btnTogglePassword);

        tvSite.setText(site);
        tvUser.setText(user);
        etPass.setText(pass);

        btnToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggle.setText("Şifreyi Göster");
            } else {
                etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggle.setText("Şifreyi Gizle");
            }
            isPasswordVisible = !isPasswordVisible;
            etPass.setSelection(etPass.getText().length());
        });

        // Kopyalama özelliği (Opsiyonel butonu XML'e eklediysen)
        // copyToClipboard(pass);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Şifre kopyalandı", Toast.LENGTH_SHORT).show();
    }
}