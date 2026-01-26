package com.example.passwordmanager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String username;
    public String password;
    public String category;

    // Room için gerekli boş constructor
    public Account() {}

    public Account(String title, String username, String password, String category) {
        this.title = title;
        this.username = username;
        this.password = password;
        this.category = category;
    }
}