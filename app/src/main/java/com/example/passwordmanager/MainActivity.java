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

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddAccountDialog());
        }

        ensureDefaultCategoriesAndLoad();
    }

    private void ensureDefaultCategoriesAndLoad() {
        new Thread(() -> {
            // CategoryDao içindeki doğru metod (getAll) çağrıldı
            if (db.categoryDao().getAll().isEmpty()) {
                db.categoryDao().insert(new Category("Sosyal Medya", false));
                db.categoryDao().insert(new Category("E-Posta", false));
                db.categoryDao().insert(new Category("Banka", false));
            }
            updateAccountList();
        }).start();
    }

    private void updateAccountList() {
        new Thread(() -> {
            List<Account> accounts = db.accountDao().getAll();
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new AccountAdapter(accounts);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.updateAccounts(accounts);
                }
            });
        }).start();
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
            // HATA DÜZELTİLDİ: getAllCategoriesSync yerine getAll kullanıldı
            List<Category> categories = db.categoryDao().getAll();
            List<String> categoryNames = new ArrayList<>();
            for (Category c : categories) {
                categoryNames.add(c.name);
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoryNames);
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(catAdapter);
            });
        }).start();

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String category = (spinnerCategory.getSelectedItem() != null) ?
                    spinnerCategory.getSelectedItem().toString() : "";

            if (title.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Account newAccount = new Account(title, username, password, category);
                db.accountDao().insert(newAccount);

                updateAccountList();
                runOnUiThread(dialog::dismiss);
            }).start();
        });

        dialog.show();
    }
}