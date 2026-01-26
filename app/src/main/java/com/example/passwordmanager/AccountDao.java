package com.example.passwordmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM accounts")
    List<Account> getAll();

    @Query("SELECT * FROM accounts WHERE category = :categoryName")
    List<Account> getAccountsByCategory(String categoryName);

    // Arama özelliği için eklenen sorgu
    @Query("SELECT * FROM accounts WHERE title LIKE :query")
    List<Account> searchAccounts(String query);

    @Insert
    void insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("DELETE FROM accounts WHERE id = :id")
    void deleteById(int id);
}