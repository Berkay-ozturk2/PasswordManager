package com.example.passwordmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM accounts")
    List<Account> getAll();

    @Query("SELECT * FROM accounts WHERE category = :categoryName")
    List<Account> getAccountsByCategory(String categoryName);

    @Insert
    void insert(Account account);

    @Delete
    void delete(Account account);
}