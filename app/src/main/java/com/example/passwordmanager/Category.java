package com.example.passwordmanager;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public boolean isHidden; // Gizli klasör mü?

    public Category(String name, boolean isHidden) {
        this.name = name;
        this.isHidden = isHidden;
    }
}