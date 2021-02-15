package com.floatingpanda.productlist.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductDao;
import com.floatingpanda.productlist.db.ProductWithCategory;

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

    public LiveData<List<ProductWithCategory>> getAllProductsWithCategory() {
        return productDao.getProductsWithCategoryOrderedByName();
    }

    public LiveData<List<ProductWithCategory>> getAllProductsWithCategoryOrderedByBarcode() {
        return productDao.getProductsWithCategoryOrderedByBarcode();
    }

    public LiveData<ProductWithCategory> getProductWithCategoryWithBarcode(long productId) {
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

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategory(long categoryId) {
        return productDao.getProductsWithCategoryByCategoryId(categoryId);
    }
}
