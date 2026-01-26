package com.example.passwordmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;
    private SharedPreferences prefs;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFolders; // Yeni eklenen dosya simgesi butonu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Veritabanı başlatma
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "sifre-kasasi")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        prefs = getSharedPreferences("SecurityPrefs", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sağdaki + Butonu (Hesap ve Klasör Ekleme için)
        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddOptionsDialog());

        // Soldaki Dosya Butonu (Klasörleri Listeleme/Filtreleme için)
        fabFolders = findViewById(R.id.fabFolders);
        fabFolders.setOnClickListener(v -> showFolderFilterDialog());

        checkMasterPassword();

        // Varsayılan kategoriler
        if (db.categoryDao().getAll().isEmpty()) {
            db.categoryDao().insert(new Category("Genel", false));
            db.categoryDao().insert(new Category("Gizli Klasör", true));
        }

        updateAccountList();
    }

    // + Butonuna basıldığında seçenekleri gösteren dialog
    private void showAddOptionsDialog() {
        String[] options = {"Yeni Hesap Ekle", "Yeni Klasör Oluştur"};
        new AlertDialog.Builder(this)
                .setTitle("Ekleme Seçenekleri")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddAccountDialog();
                    } else {
                        showAddFolderDialog();
                    }
                })
                .show();
    }

    // Klasörleri listeleme ve filtreleme dialogu (Artık sol butona bağlı)
    private void showFolderFilterDialog() {
        List<Category> categories = db.categoryDao().getAll();
        List<String> catNames = new ArrayList<>();
        catNames.add("Tüm Hesaplar"); // Varsayılan seçenek
        for (Category c : categories) {
            catNames.add(c.name + (c.isHidden ? " (Gizli)" : ""));
        }

        new AlertDialog.Builder(this)
                .setTitle("Görüntülenecek Klasörü Seçin")
                .setItems(catNames.toArray(new String[0]), (dialog, which) -> {
                    if (which == 0) {
                        updateAccountList(); // Tümünü göster
                    } else {
                        Category selectedCat = categories.get(which - 1);
                        if (selectedCat.isHidden) {
                            askMasterPasswordForFolder(selectedCat.id);
                        } else {
                            loadAccountsByCategory(selectedCat.id);
                        }
                    }
                })
                .show();
    }

    // Gizli klasörler için şifre sorma
    private void askMasterPasswordForFolder(int categoryId) {
        final EditText input = new EditText(this);
        input.setHint("Ana Şifre");
        new AlertDialog.Builder(this)
                .setTitle("Güvenlik")
                .setMessage("Bu klasöre erişmek için ana şifrenizi girin.")
                .setView(input)
                .setPositiveButton("Giriş", (d, w) -> {
                    String pass = input.getText().toString();
                    String savedPass = prefs.getString("master_password", "");
                    if (pass.equals(savedPass)) {
                        loadAccountsByCategory(categoryId);
                    } else {
                        Toast.makeText(this, "Hatalı şifre!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    // Yeni Klasör Oluşturma Dialogu
    private void showAddFolderDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etFolderName = new EditText(this);
        etFolderName.setHint("Klasör Adı");
        layout.addView(etFolderName);

        final CheckBox cbIsHidden = new CheckBox(this);
        cbIsHidden.setText("Gizli Klasör (Şifreli)");
        layout.addView(cbIsHidden);

        new AlertDialog.Builder(this)
                .setTitle("Yeni Klasör")
                .setView(layout)
                .setPositiveButton("Oluştur", (dialog, which) -> {
                    String name = etFolderName.getText().toString();
                    if (!name.isEmpty()) {
                        Category newCat = new Category(name, cbIsHidden.isChecked());
                        db.categoryDao().insert(newCat);
                        Toast.makeText(this, "Klasör oluşturuldu!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    // Yeni Hesap Ekleme Dialogu
    private void showAddAccountDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etSite = new EditText(this);
        etSite.setHint("Site Adı");
        layout.addView(etSite);

        final EditText etUser = new EditText(this);
        etUser.setHint("Kullanıcı Adı");
        layout.addView(etUser);

        final EditText etPass = new EditText(this);
        etPass.setHint("Şifre");
        layout.addView(etPass);

        // Kategori seçimi için Spinner
        final Spinner spinner = new Spinner(this);
        List<Category> categories = db.categoryDao().getAll();
        List<String> catNames = new ArrayList<>();
        for (Category c : categories) catNames.add(c.name);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, catNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        layout.addView(spinner);

        new AlertDialog.Builder(this)
                .setTitle("Yeni Hesap Bilgisi")
                .setView(layout)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String site = etSite.getText().toString();
                    String user = etUser.getText().toString();
                    String pass = etPass.getText().toString();
                    int catId = categories.get(spinner.getSelectedItemPosition()).id;

                    if (!site.isEmpty() && !pass.isEmpty()) {
                        Account account = new Account(site, user, pass, catId);
                        db.accountDao().insert(account);
                        updateAccountList();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
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
                    } else {
                        showCreatePasswordDialog();
                    }
                }).show();
    }

    private void updateAccountList() {
        List<Account> accounts = db.accountDao().getAll();
        AccountAdapter adapter = new AccountAdapter(accounts);
        recyclerView.setAdapter(adapter);
    }

    public void loadAccountsByCategory(int categoryId) {
        List<Account> accounts = db.accountDao().getAccountsByCategory(categoryId);
        AccountAdapter adapter = new AccountAdapter(accounts);
        recyclerView.setAdapter(adapter);
    }
}