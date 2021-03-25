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
    @Query("SELECT * FROM products")
    LiveData<List<ProductWithCategory>> getProductsWithCategory();

    //TODO remove commented out sections
    /*
    @Transaction
    @Query("SELECT * FROM products ORDER BY name DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByNameDesc();

    @Transaction
    @Query("SELECT * FROM products ORDER BY barcode ASC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByBarcodeAsc();

    @Transaction
    @Query("SELECT * FROM products ORDER BY barcode DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByBarcodeDesc();

    @Transaction
    @Query("SELECT * FROM products ORDER BY price ASC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByPriceAsc();

    @Transaction
    @Query("SELECT * FROM products ORDER BY price DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryOrderedByPriceDesc();

     */

    @Transaction
    @Query("SELECT * FROM products WHERE id LIKE :id")
    LiveData<ProductWithCategory> getProductWithCategoryByProductId(long id);

    @Transaction
    @Query("SELECT * FROM products WHERE barcode LIKE :barcode")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByExactBarcode(String barcode);

    /*
    @Transaction
    @Query("SELECT * FROM products WHERE barcode LIKE :barcode ORDER BY name DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByExactBarcodeOrderedByNameDesc(String barcode);

    //TODO write product dao test
    @Transaction
    @Query("SELECT * FROM products WHERE barcode LIKE :barcode ORDER BY price ASC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByExactBarcodeOrderedByPriceAsc(String barcode);

    @Transaction
    @Query("SELECT * FROM products WHERE barcode LIKE :barcode ORDER BY price DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByExactBarcodeOrderedByPriceDesc(String barcode);


     */

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryId(long categoryId);

    /*
    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId ORDER BY name DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdOrderedByNameDesc(long categoryId);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId ORDER BY barcode ASC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdOrderedByBarcodeAsc(long categoryId);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId ORDER BY barcode DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdOrderedByBarcodeDesc(long categoryId);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId ORDER BY price ASC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdOrderedByPriceAsc(long categoryId);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId ORDER BY price DESC")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdOrderedByPriceDesc(long categoryId);

     */

    @Transaction
    @RawQuery
    LiveData<List<ProductWithCategory>> searchProductsWithCategory(SupportSQLiteQuery query);
}
