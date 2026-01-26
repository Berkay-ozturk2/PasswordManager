package com.example.passwordmanager;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Versiyonu 2 yapıyoruz çünkü tablo yapısı değişti
@Database(entities = {Account.class, Category.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AccountDao accountDao();
    public abstract CategoryDao categoryDao();
}