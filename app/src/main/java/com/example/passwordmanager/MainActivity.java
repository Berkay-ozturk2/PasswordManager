package com.example.passwordmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "sifre-kasasi").build();
        prefs = getSharedPreferences("SecurityPrefs", MODE_PRIVATE);

        checkMasterPassword(); // İlk açılışta şifre kontrolü
    }

    private void checkMasterPassword() {
        String savedPass = prefs.getString("master_password", null);

        if (savedPass == null) {
            // İlk kez giriliyorsa şifre belirlet
            showCreatePasswordDialog();
        } else {
            // Şifre varsa girişte sor (Bunu her açılışta veya gizli klasörde yapabilirsin)
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
                    prefs.edit().putString("master_password", input.getText().toString()).apply();
                    Toast.makeText(this, "Şifre oluşturuldu!", Toast.LENGTH_SHORT).show();
                }).show();
    }

    // Gizli bir klasöre tıklandığında çağrılacak metod
    private void openHiddenFolder(Category category) {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Şifre Gerekli")
                .setView(input)
                .setPositiveButton("Giriş", (d, w) -> {
                    String pass = prefs.getString("master_password", "");
                    if (input.getText().toString().equals(pass)) {
                        // Şifre doğru, klasör içeriğini göster
                    } else {
                        Toast.makeText(this, "Hatalı şifre!", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }
}