package com.example.passwordmanager;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Değişkenler metodun dışında tanımlanmalı ki her yerden ulaşılsın
    private AppDatabase db;
    private RecyclerView recyclerView;
    private AccountAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Önce ekranı oluştur
        setContentView(R.layout.activity_main);

        // 2. Veritabanını başlat (UpdateList'ten önce olmalı!)
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "sifre-veritabani")
                .allowMainThreadQueries()
                .build();

        // 3. Görselleri (View) bağla
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 4. Listeyi doldur
        updateList();

        // Buton ve Tıklama Dinleyicisi
        com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> {
            // Şimdilik test verisi yerine hazırladığımız diyaloğu açalım
            showAddAccountDialog();
        });
    }

    private void showAddAccountDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.EditText inputSite = new android.widget.EditText(this);
        inputSite.setHint("Site veya Uygulama Adı");
        layout.addView(inputSite);

        final android.widget.EditText inputUser = new android.widget.EditText(this);
        inputUser.setHint("Kullanıcı Adı");
        layout.addView(inputUser);

        final android.widget.EditText inputPass = new android.widget.EditText(this);
        inputPass.setHint("Şifre");
        inputPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPass);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Yeni Hesap Ekle")
                .setView(layout)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String site = inputSite.getText().toString();
                    String user = inputUser.getText().toString();
                    String pass = inputPass.getText().toString();

                    if (!site.isEmpty() && !pass.isEmpty()) {
                        Account newAccount = new Account(site, user, pass);
                        db.accountDao().insert(newAccount);
                        Toast.makeText(this, "Kaydedildi!", Toast.LENGTH_SHORT).show();

                        // ÖNEMLİ: Veri eklenince listeyi yenilemelisin
                        updateList();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void updateList() {
        // Veritabanından güncel verileri çek
        List<Account> accounts = db.accountDao().getAll();
        // Adapter'ı oluştur ve RecyclerView'a bağla
        adapter = new AccountAdapter(accounts);
        recyclerView.setAdapter(adapter);
    }
}