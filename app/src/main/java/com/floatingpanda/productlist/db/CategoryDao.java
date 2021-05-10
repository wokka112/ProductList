package com.floatingpanda.productlist.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {

    // Returns all categories in alphabetic order
    @Query("SELECT * FROM categories ORDER BY name")
    LiveData<List<Category>> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Category category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMultiple(Category... categories);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Delete
    void deleteMultiple(Category... categories);

    @Query("DELETE from categories")
    void deleteAll();

    //TODO write test
    @Query("SELECT EXISTS(SELECT * FROM categories WHERE name LIKE :categoryName)")
    boolean containsCategory(String categoryName);
}
