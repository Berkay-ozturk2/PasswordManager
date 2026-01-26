package com.example.passwordmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;
    private SharedPreferences prefs;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Room import hatası burada düzeltildi
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "sifre-kasasi")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration() // Şema değiştiği için veritabanını temizler
                .build();

        prefs = getSharedPreferences("SecurityPrefs", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        checkMasterPassword();

        // Örnek: Varsayılan bir kategori yoksa oluştur (Test için)
        if (db.categoryDao().getAll().isEmpty()) {
            db.categoryDao().insert(new Category("Genel", false));
            db.categoryDao().insert(new Category("Gizli Klasör", true));
        }

        updateCategoryList();
    }

    private void checkMasterPassword() {
        String savedPass = prefs.getString("master_password", null);
        if (savedPass == null) {
            showCreatePasswordDialog();
        }
    }

    private void showCreatePasswordDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Ana Şifre Belirle")
                .setMessage("Gizli klasörlere erişmek için bir şifre seçin.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Kaydet", (d, w) -> {
                    String pass = input.getText().toString();
                    if (!pass.isEmpty()) {
                        prefs.edit().putString("master_password", pass).apply();
                        Toast.makeText(this, "Şifre oluşturuldu!", Toast.LENGTH_SHORT).show();
                    } else {
                        showCreatePasswordDialog(); // Şifre boş olamaz
                    }
                }).show();
    }

    private void updateCategoryList() {
        // Burada kategorileri listeleyen bir Adapter kullanmalısın
        // Şimdilik sadece tüm hesapları gösteren eski mantığı koruyabiliriz
        List<Account> accounts = db.accountDao().getAll();
        AccountAdapter adapter = new AccountAdapter(accounts);
        recyclerView.setAdapter(adapter);
    }

    public void openHiddenFolder(Category category) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Şifre Gerekli")
                .setMessage(category.name + " klasörüne erişmek için ana şifreyi girin.")
                .setView(input)
                .setPositiveButton("Giriş", (d, w) -> {
                    String pass = prefs.getString("master_password", "");
                    if (input.getText().toString().equals(pass)) {
                        // Şifre doğru, bu kategoriye ait hesapları filtrele
                        loadAccountsByCategory(category.id);
                    } else {
                        Toast.makeText(this, "Hatalı şifre!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void loadAccountsByCategory(int categoryId) {
        List<Account> accounts = db.accountDao().getAccountsByCategory(categoryId);
        AccountAdapter adapter = new AccountAdapter(accounts);
        recyclerView.setAdapter(adapter);
    }
}