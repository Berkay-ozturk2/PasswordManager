package com.example.passwordmanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddAccountDialog());
        findViewById(R.id.fabFolders).setOnClickListener(v -> showCategoryPopupMenu(v));

        ensureDefaultCategoriesAndLoad();
    }

    private void showCategoryPopupMenu(View view) {
        new Thread(() -> {
            List<Category> categories = db.categoryDao().getAll();
            runOnUiThread(() -> {
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.getMenu().add(0, 0, 0, "+ Yeni Kategori Ekle");
                popupMenu.getMenu().add(0, 1, 1, "Tümü");
                for (Category c : categories) {
                    popupMenu.getMenu().add(0, c.id + 100, 2, c.name);
                }
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) showAddCategoryDialog();
                    else if (item.getItemId() == 1) updateAccountList();
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
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Kategori eklendi", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        });
        dialog.show();
    }

    private void filterAccountsByCategory(String categoryName) {
        new Thread(() -> {
            List<Account> filtered = db.accountDao().getAccountsByCategory(categoryName);
            runOnUiThread(() -> {
                if (adapter != null) adapter.updateAccounts(filtered);
            });
        }).start();
    }

    private void updateAccountList() {
        new Thread(() -> {
            List<Account> accounts = db.accountDao().getAll();
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new AccountAdapter(accounts);
                    recyclerView.setAdapter(adapter);
                } else adapter.updateAccounts(accounts);
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

    @Override
    protected void onResume() {
        super.onResume();
        updateAccountList();
    }

    private void showAddAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

        builder.setView(view);
        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            Object selected = spinnerCategory.getSelectedItem();
            String cat = (selected != null) ? selected.toString() : "Genel";

            if (!title.isEmpty() && !user.isEmpty() && !pass.isEmpty()) {
                new Thread(() -> {
                    db.accountDao().insert(new Account(title, user, pass, cat));
                    updateAccountList();
                    runOnUiThread(dialog::dismiss);
                }).start();
            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}