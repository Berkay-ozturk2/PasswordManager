package com.example.passwordmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private AppDatabase db;
    private FloatingActionButton fabAdd, fabFolders;

    // Şu an seçili olan kategori ID'sini tutar (-1 ise tüm hesaplar gösterilir)
    private int selectedCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Veritabanı bağlantısı
        db = AppDatabase.getInstance(this);

        // Görünümleri tanımlama
        recyclerView = findViewById(R.id.recyclerViewAccounts);
        fabAdd = findViewById(R.id.fabAdd);
        fabFolders = findViewById(R.id.fabFolders);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // Başlangıçta verileri kontrol et ve yükle
        ensureDefaultCategoriesAndLoad();

        // Buton tıklama olayları
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddAccountDialog());
        }

        if (fabFolders != null) {
            fabFolders.setOnClickListener(v -> showFolderPopupMenu(v));
        }
    }

    // Kategorileri kontrol eder, yoksa ekler ve ardından listeyi yükler
    private void ensureDefaultCategoriesAndLoad() {
        new Thread(() -> {
            try {
                if (db.categoryDao().getAll().isEmpty()) {
                    // Category sınıfınızın constructor'ı sadece 'name' beklediği için fazladan parametre silindi
                    db.categoryDao().insert(new Category("Sosyal Medya"));
                    db.categoryDao().insert(new Category("Banka"));
                    db.categoryDao().insert(new Category("E-Posta"));
                    db.categoryDao().insert(new Category("Oyun"));
                }
                // Kategoriler hazır olduktan sonra listeyi güncelle
                updateAccountList();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Veritabanı hatası: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    // Listeyi seçili kategoriye göre günceller
    private void updateAccountList() {
        new Thread(() -> {
            try {
                final List<Account> accounts;
                if (selectedCategoryId == -1) {
                    accounts = db.accountDao().getAll();
                } else {
                    accounts = db.accountDao().getAccountsByCategory(selectedCategoryId);
                }

                runOnUiThread(() -> {
                    // AccountAdapter hem liste hem de Context beklediği için MainActivity.this eklendi
                    adapter = new AccountAdapter(accounts, MainActivity.this);
                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Liste yüklenemedi", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Sol üstteki buton için PopupMenu (Açılır Menü)
    private void showFolderPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);

        new Thread(() -> {
            try {
                List<Category> categories = db.categoryDao().getAll();

                runOnUiThread(() -> {
                    popup.getMenu().add(0, -1, 0, "Tüm Hesaplar");

                    for (int i = 0; i < categories.size(); i++) {
                        Category cat = categories.get(i);
                        // Item ID olarak kategori ID'sini atıyoruz
                        popup.getMenu().add(0, cat.id, i + 1, cat.name);
                    }

                    popup.setOnMenuItemClickListener(item -> {
                        selectedCategoryId = item.getItemId();
                        updateAccountList();
                        Toast.makeText(MainActivity.this, item.getTitle() + " filtrelendi", Toast.LENGTH_SHORT).show();
                        return true;
                    });

                    popup.show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Klasörler yüklenemedi", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Yeni hesap ekleme diyaloğu
    private void showAddAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_detail2, null);
        builder.setView(view);

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button btnSave = view.findViewById(R.id.btnSave);

        AlertDialog dialog = builder.create();

        new Thread(() -> {
            try {
                final List<Category> categories = db.categoryDao().getAll();
                List<String> categoryNames = new ArrayList<>();
                for (Category c : categories) {
                    categoryNames.add(c.name);
                }

                runOnUiThread(() -> {
                    if (categories.isEmpty()) {
                        Toast.makeText(this, "Önce klasör oluşturulmalı", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }

                    if (spinnerCategory != null) {
                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategory.setAdapter(spinnerAdapter);

                        // Filtreli bir klasördeysek, o klasörü otomatik seç
                        if (selectedCategoryId != -1) {
                            for (int i = 0; i < categories.size(); i++) {
                                if (categories.get(i).id == selectedCategoryId) {
                                    spinnerCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    if (btnSave != null) {
                        btnSave.setOnClickListener(v -> {
                            String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                            String user = etUsername != null ? etUsername.getText().toString().trim() : "";
                            String pass = etPassword != null ? etPassword.getText().toString().trim() : "";

                            if (title.isEmpty() || pass.isEmpty()) {
                                Toast.makeText(this, "Başlık ve Şifre boş bırakılamaz", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int selectedPos = spinnerCategory != null ? spinnerCategory.getSelectedItemPosition() : 0;
                            if (selectedPos < 0) return;

                            int catId = categories.get(selectedPos).id;

                            new Thread(() -> {
                                try {
                                    db.accountDao().insert(new Account(title, user, pass, catId));
                                    runOnUiThread(() -> {
                                        updateAccountList();
                                        dialog.dismiss();
                                        Toast.makeText(this, "Başarıyla kaydedildi", Toast.LENGTH_SHORT).show();
                                    });
                                } catch (Exception ex) {
                                    runOnUiThread(() -> Toast.makeText(this, "Kayıt hatası!", Toast.LENGTH_SHORT).show());
                                }
                            }).start();
                        });
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Veri yükleme hatası", Toast.LENGTH_SHORT).show());
            }
        }).start();

        dialog.show();
    }
}