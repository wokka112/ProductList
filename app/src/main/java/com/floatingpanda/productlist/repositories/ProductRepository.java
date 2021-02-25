package com.floatingpanda.productlist.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductDao;
import com.floatingpanda.productlist.db.ProductWithCategory;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private ProductDao productDao;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
    }

    // Used for tests
    public ProductRepository(AppDatabase appDatabase) {
        productDao = appDatabase.productDao();
    }

    public LiveData<List<ProductWithCategory>> getAllProductsWithCategoryOrderedByName() {
        return productDao.getProductsWithCategoryOrderedByName();
    }

    public LiveData<List<ProductWithCategory>> getAllProductsWithCategoryOrderedByBarcode() {
        return productDao.getProductsWithCategoryOrderedByBarcode();
    }

    public LiveData<ProductWithCategory> getProductWithCategoryByProductId(long productId) {
        return productDao.getProductWithCategoryByProductId(productId);
    }

    public void addProduct(Product product) {
        AppDatabase.getExecutorService().execute(() -> {
            productDao.insert(product);
        });
    }

    public void addProducts(Product... products) {
        AppDatabase.getExecutorService().execute(() -> {
            productDao.insertMultiple(products);
        });
    }

    public void editProduct(Product product) {
        AppDatabase.getExecutorService().execute(() -> {
            productDao.update(product);
        });
    }

    public void deleteProduct(Product product) {
        AppDatabase.getExecutorService().execute(() -> {
            productDao.delete(product);
        });
    }

    public void deleteProducts(Product... products) {
        AppDatabase.getExecutorService().execute(() -> {
            productDao.deleteMultiple(products);
        });
    }

    public void deleteAllProducts() {
        AppDatabase.getExecutorService().execute(() -> {
            productDao.deleteAll();
        });
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryByBarcode(String barcode) {
        return productDao.getProductsWithCategoryByBarcode(barcode);
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryId(long categoryId) {
        return productDao.getProductsWithCategoryByCategoryId(categoryId);
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryContainingName(String name) {
        return productDao.getProductsWithCategoryContainingName(name);
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryBetweenTwoPrices(float lowerPrice, float higherPrice) {
        return productDao.getProductsWithCategoryBetweenTwoPrices(lowerPrice, higherPrice);
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryByPrice(float price) {
        return getProductsWithCategoryBetweenTwoPrices(price, price);
    }

    //TODO look into whether we can pipe searches in SQL and Room, or make a single SQL query that
    // can take multiple elements.
    // If not, add different search types like below.
    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, float lowerPrice, float higherPrice) {
        String queryString = "SELECT * FROM products";

        List<Object> args = new ArrayList<>();

        boolean whereStarted = false;

        if (barcode != null && !barcode.trim().isEmpty()) {
            queryString += " WHERE barcode LIKE ? || '%'";
            args.add(barcode);
            whereStarted = true;
        }

        if (name != null && !name.trim().isEmpty()) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " Upper(name) LIKE '%' || Upper(?) || '%'";
            args.add(name);
        }

        if (categoryId > 0) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " category_id LIKE ?";
            args.add(categoryId);
        }

        //TODO fix floats in table, they're causing really bad comparison issues
        if (lowerPrice >= 0f && higherPrice >= lowerPrice) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " price >= ?";
            args.add(lowerPrice);
            queryString += " AND price <= ?";
            args.add(higherPrice);
        }
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString, args.toArray());
        return productDao.searchProductsWithCategory(query);
    }

    /*
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
     */
}
