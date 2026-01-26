package com.example.passwordmanager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String siteName;
    public String username;
    public String password;
    public int categoryId; // Hangi klasörde olduğunu tutar

    public Account(String siteName, String username, String password, int categoryId) {
        this.siteName = siteName;
        this.username = username;
        this.password = password;
        this.categoryId = categoryId;
    }
}