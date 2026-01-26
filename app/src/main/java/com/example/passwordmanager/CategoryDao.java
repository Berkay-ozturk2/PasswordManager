package com.example.passwordmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories")
    List<Category> getAll();

    @Insert
    void insert(Category category);

    @Delete
    void delete(Category category);

    @Query("DELETE FROM categories WHERE name = :categoryName")
    void deleteByName(String categoryName);
}