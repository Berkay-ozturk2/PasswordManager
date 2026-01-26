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
        ((TextView)findViewById(R.id.tvDetailTitle)).setText(getIntent().getStringExtra("title"));
        ((TextView)findViewById(R.id.tvDetailUsername)).setText(getIntent().getStringExtra("username"));
        ((TextView)findViewById(R.id.tvDetailPassword)).setText(getIntent().getStringExtra("password"));

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sil")
                    .setMessage("Emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(this).accountDao().deleteById(id);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Silindi", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }).start();
                    })
                    .setNegativeButton("Hayır", null).show();
        });
    }
}