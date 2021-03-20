package com.floatingpanda.productlist.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT * FROM products")
    LiveData<List<Product>> getAll();

    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getProductById(long id);

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

    @Transaction
    @Query("SELECT * FROM products ORDER BY barcode")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByBarcode();

    @Transaction
    @Query("SELECT * FROM products ORDER BY name")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByName();

    @Transaction
    @Query("SELECT * FROM products ORDER BY price")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByPrice();

    @Transaction
    @Query("SELECT * FROM products WHERE id LIKE :id")
    LiveData<ProductWithCategory> getProductWithCategoryByProductId(long id);

    @Transaction
    @Query("SELECT * FROM products WHERE barcode LIKE :barcode ORDER BY name")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByExactBarcode(String barcode);

    //TODO add a method to getProductsWithCategoryByExactBarcode ordered by price???

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryId(long categoryId);

    @Transaction
    @RawQuery
    LiveData<List<ProductWithCategory>> searchProductsWithCategory(SupportSQLiteQuery query);
}
