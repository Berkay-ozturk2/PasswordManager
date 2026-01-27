package com.example.passwordmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.splashscreen.SplashScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private AppDatabase db;
    private View mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // GÜVENLİ EKRAN: Ekran görüntüsü engelleme
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.recyclerView);
        mainLayout.setVisibility(View.GONE);

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupUI();
        checkAuthentication();
    }

    private void checkAuthentication() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Giriş Hatası: " + errString, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> {
                    mainLayout.setVisibility(View.VISIBLE);
                    ensureDefaultCategoriesAndLoad();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Uygulamaya Giriş")
                .setSubtitle("Biyometrik veri veya cihaz şifresi ile giriş yapın")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupUI() {
        SearchView searchView = findViewById(R.id.searchView);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { performSearch(query); return true; }
                @Override public boolean onQueryTextChange(String newText) { performSearch(newText); return true; }
            });
        }
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddAccountDialog());
        // Hatanın çözümü: showCategoryPopupMenu metodu artık sınıf içinde tanımlı
        findViewById(R.id.fabFolders).setOnClickListener(v -> showCategoryPopupMenu(v));
    }

    private void performSearch(String query) {
        new Thread(() -> {
            List<Account> result;
            if (query.isEmpty()) { result = db.accountDao().getAll(); }
            else {
                List<Account> allAccounts = db.accountDao().getAll();
                result = new ArrayList<>();
                Locale trLocale = new Locale("tr", "TR");
                String lowerQuery = query.toLowerCase(trLocale);
                for (Account acc : allAccounts) {
                    if (acc.title.toLowerCase(trLocale).contains(lowerQuery)) { result.add(acc); }
                }
            }
            runOnUiThread(() -> { if (adapter != null) adapter.updateAccounts(result); });
        }).start();
    }

    private void showCategoryPopupMenu(View view) {
        new Thread(() -> {
            List<Category> categories = db.categoryDao().getAll();
            runOnUiThread(() -> {
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.getMenu().add(0, 0, 0, "+ Yeni Kategori Ekle");
                popupMenu.getMenu().add(0, 1, 1, "- Kategori Sil");
                popupMenu.getMenu().add(0, 2, 2, "Tümü");
                for (Category c : categories) { popupMenu.getMenu().add(0, c.id + 100, 3, c.name); }
                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == 0) showAddCategoryDialog();
                    else if (id == 1) showDeleteCategoryDialog();
                    else if (id == 2) updateAccountList();
                    else filterAccountsByCategory(item.getTitle().toString());
                    return true;
                });
                popupMenu.show();
            });
        }).start();
    }

    private void showAddCategoryDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(v).create();
        v.findViewById(R.id.btnSaveCategory).setOnClickListener(view -> {
            String name = ((EditText)v.findViewById(R.id.etCategoryName)).getText().toString().trim();
            if (!name.isEmpty()) {
                new Thread(() -> {
                    db.categoryDao().insert(new Category(name, false));
                    runOnUiThread(() -> { dialog.dismiss(); Toast.makeText(this, "Kategori eklendi", Toast.LENGTH_SHORT).show(); });
                }).start();
            }
        });
        dialog.show();
    }

    private void showDeleteCategoryDialog() {
        new Thread(() -> {
            List<Category> categories = db.categoryDao().getAll();
            List<String> names = new ArrayList<>();
            for (Category c : categories) names.add(c.name);
            runOnUiThread(() -> {
                if (names.isEmpty()) return;
                new AlertDialog.Builder(this).setTitle("Kategori Sil").setItems(names.toArray(new String[0]), (dialog, which) -> {
                    String selected = names.get(which);
                    new Thread(() -> {
                        db.categoryDao().deleteByName(selected);
                        runOnUiThread(() -> { Toast.makeText(this, "Kategori silindi", Toast.LENGTH_SHORT).show(); updateAccountList(); });
                    }).start();
                }).show();
            });
        }).start();
    }

    private void filterAccountsByCategory(String categoryName) {
        new Thread(() -> {
            List<Account> filtered = db.accountDao().getAccountsByCategory(categoryName);
            runOnUiThread(() -> { if (adapter != null) adapter.updateAccounts(filtered); });
        }).start();
    }

    private void updateAccountList() {
        new Thread(() -> {
            List<Account> accounts = db.accountDao().getAll();
            runOnUiThread(() -> {
                if (adapter == null) { adapter = new AccountAdapter(accounts); recyclerView.setAdapter(adapter); }
                else adapter.updateAccounts(accounts);
            });
        }).start();
    }

    private void ensureDefaultCategoriesAndLoad() {
        new Thread(() -> {
            if (db.categoryDao().getAll().isEmpty()) {
                db.categoryDao().insert(new Category("Sosyal Medya", false));
                db.categoryDao().insert(new Category("Banka", false));
            }
            updateAccountList();
        }).start();
    }

    @Override protected void onResume() { super.onResume(); }

    private void showAddAccountDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button btnSave = view.findViewById(R.id.btnSave);
        new Thread(() -> {
            List<Category> categories = db.categoryDao().getAll();
            List<String> names = new ArrayList<>();
            for (Category c : categories) names.add(c.name);
            runOnUiThread(() -> {
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(catAdapter);
            });
        }).start();
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            Object selected = spinnerCategory.getSelectedItem();
            String cat = (selected != null) ? selected.toString() : "Genel";
            if (!title.isEmpty() && !user.isEmpty() && !pass.isEmpty()) {
                new Thread(() -> {
                    // Singleton CryptoHelper kullanımı
                    String encryptedPass = CryptoHelper.getInstance().encrypt(pass);
                    db.accountDao().insert(new Account(title, user, encryptedPass, cat));
                    updateAccountList();
                    runOnUiThread(dialog::dismiss);
                }).start();
            }
        });
        dialog.show();
    }
}