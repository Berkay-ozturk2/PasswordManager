package com.example.passwordmanager;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail2);

        // Verileri al
        String title = getIntent().getStringExtra("title");
        String user = getIntent().getStringExtra("username");
        String pass = getIntent().getStringExtra("password");

        TextView tvSite = findViewById(R.id.detailSiteName);
        TextView tvUser = findViewById(R.id.detailUsername);
        EditText etPass = findViewById(R.id.detailPassword);
        Button btnToggle = findViewById(R.id.btnTogglePassword);

        tvSite.setText(title);
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
    }
}