package com.floatingpanda.productlist.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

//TODO write tests
@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories")
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
}
