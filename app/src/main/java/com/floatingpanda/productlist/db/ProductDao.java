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

@Dao
public interface ProductDao {

    @Query("SELECT * FROM products")
    LiveData<List<Product>> getAll();

    //TODO write test
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

    //TODO write tests for everything below this point
    @Transaction
    @Query("SELECT * FROM products WHERE id LIKE :id")
    LiveData<ProductWithCategory> getProductWithCategoryByProductId(long id);

    @Transaction
    @Query("SELECT * FROM products WHERE barcode LIKE :barcode || '%'")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByBarcode(String barcode);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryId(long categoryId);

    @Transaction
    @Query("SELECT * FROM products WHERE name LIKE '%' || :name || '%'")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryContainingName(String name);

    // Inclusive on both sides
    // Use same price for lower and higher to get a specific price.
    @Transaction
    @Query("SELECT * FROM products WHERE price >= :lowerPrice AND price <= :higherPrice")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryBetweenTwoPrices(float lowerPrice, float higherPrice);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId AND name LIKE '%' || :name || '%'")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdAndContainingName(
            long categoryId, String name);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId AND price >= :lowerPrice AND price <= :higherPrice")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdAndBetweenTwoPrices(
            long categoryId, float lowerPrice, float higherPrice);

    @Transaction
    @Query("SELECT * FROM products WHERE name LIKE '%' || :name || '%' AND price >= :lowerPrice AND price <= :higherPrice")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryContainingNameAndBetweenTwoPrices(
            String name, float lowerPrice, float higherPrice);

    @Transaction
    @Query("SELECT * FROM products WHERE category_id LIKE :categoryId AND name LIKE '%' || :name || '%' " +
            "AND price >= :lowerPrice AND price <= :higherPrice")
    LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryIdAndContainingNameAndBetweenPrices(
            long categoryId, String name, float lowerPrice, float higherPrice);

    //TODO include searches involving barcodes

    //TODO look into whether I can pipe searches somehow to reduce number of methods
    @Query("SELECT * FROM products WHERE IF(:categoryId != null, category_id LIKE :categoryId, category_id LIKE *) AND " +
            "")
    LiveData<List<ProductWithCategory>> searchProductsWithCategories(long categoryId, String barcode,
            String name, float lowerPrice, float higherPrice);
    // SELECT * FROM products WHERE category_id LIKE IF) category_id LIKE :categoryId
}
