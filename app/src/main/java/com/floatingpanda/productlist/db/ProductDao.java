package com.floatingpanda.productlist.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

//TODO write tests
@Dao
public interface ProductDao {

    @Query("SELECT * FROM products")
    LiveData<List<Product>> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Product product);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMultiple(Product... products);

    @Update
    void update(Product product);

    @Update
    void updateMultiple(Product... products);

    @Delete
    void delete(Product product);

    @Delete
    void deleteMultiple(Product... products);

    @Query("DELETE FROM products")
    void deleteAll();

    @Query("SELECT * FROM products WHERE barcode LIKE :barcode")
    LiveData<List<Product>> getProductsByBarcode(String barcode);

    //TODO change to find results containing name, rather than perfectly matching name, and write tests
    // Maybe also add a ProductWithCategory version.
    @Query("SELECT * FROM products WHERE name LIKE :name")
    LiveData<List<Product>> getProductsContainingName(String name);

    @Transaction
    @Query("SELECT * FROM products ORDER BY barcode")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByBarcode();

    @Transaction
    @Query("SELECT * FROM products ORDER BY name")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByName();
}
