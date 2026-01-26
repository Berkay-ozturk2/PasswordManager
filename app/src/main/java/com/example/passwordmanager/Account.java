package com.example.passwordmanager; // Burayı kendi paket adınla değiştir!

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String siteName;
    public String username;
    public String password; // İleride bunu şifreleyeceğiz

    public Account(String siteName, String username, String password) {
        this.siteName = siteName;
        this.username = username;
        this.password = password;
    }
}