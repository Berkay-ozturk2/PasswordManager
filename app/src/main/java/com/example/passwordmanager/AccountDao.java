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

    @Query("SELECT * FROM accounts WHERE categoryId = :catId")
    List<Account> getAccountsByCategory(int catId);

    @Insert
    void insert(Account account);

    @Delete
    void delete(Account account);
}