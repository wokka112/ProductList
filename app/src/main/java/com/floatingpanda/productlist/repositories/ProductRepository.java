package com.floatingpanda.productlist.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.floatingpanda.productlist.db.Price;
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

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryByExactBarcode(String barcode) {
        return productDao.getProductsWithCategoryByExactBarcode(barcode);
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategoryByCategoryId(long categoryId) {
        return productDao.getProductsWithCategoryByCategoryId(categoryId);
    }

    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, float lowerPrice, float higherPrice) {
        SimpleSQLiteQuery query = createSQLQuery(barcode, name, categoryId, lowerPrice, higherPrice);
        return productDao.searchProductsWithCategory(query);
    }

    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, int lowerPrice, int higherPrice) {
        SimpleSQLiteQuery query = createSQLQuery(barcode, name, categoryId, lowerPrice, higherPrice);
        return productDao.searchProductsWithCategory(query);
    }

    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, Price lowerPrice, Price higherPrice) {
        SimpleSQLiteQuery query = createSQLQuery(barcode, name, categoryId, lowerPrice, higherPrice);
        return productDao.searchProductsWithCategory(query);
    }

    /**
     * Searches the database and returns a list of products with categories, filtered by name,
     * barcode, category id and price, or any combination of these. Partial names and partial
     * barcodes can be used.
     *
     * If barcode is set to null, products won't be filtered by barcodes.
     * If name is set to null, products won't be filtered by names.
     * If categoryId is set to 0, products won't be filtered by categories.
     * If lowerPrice is set to 0 then products will not be filtered with a lower
     * price.
     * If higherPrice is set to 0 then products will not be filtered with
     * a higher price.
     *
     * categoryId must be non-negative.
     * lowerPrice and higherPrice must be non-negative.
     * If lowerPrice is greater than 0, then higherPrice must be either greater than or equal to
     * lowerPrice or set to 0.
     *
     * @param barcode
     * @param name
     * @param categoryId
     * @param lowerPrice
     * @param higherPrice
     * @return
     */
    private SimpleSQLiteQuery createSQLQuery(String barcode, String name, long categoryId,
                                             int lowerPrice, int higherPrice) {
        String queryString = "SELECT * FROM products";

        List<Object> args = new ArrayList<>();

        // Tracks whether already using a WHERE clause in the query
        boolean whereStarted = false;

        if (barcode != null && !barcode.trim().isEmpty()) {
            queryString += " WHERE barcode LIKE '%' || ? || '%'";
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

        if (lowerPrice > 0f) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " price >= ?";

            // Prices are stored in the database as integers with a value 100 times the original price
            // (e.g. £9.99 = 999). We convert the prices to integers that we can then compare with the
            // db stored prices.
            int lowerPriceInt = Math.round(lowerPrice * 100);
            args.add(lowerPriceInt);
        }

        if (higherPrice > 0f) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " price <= ?";

            int higherPriceInt = Math.round(higherPrice * 100);
            args.add(higherPriceInt);
        }

        return new SimpleSQLiteQuery(queryString, args.toArray());
    }

    private SimpleSQLiteQuery createSQLQuery(String barcode, String name, long categoryId,
            float lowerPrice, float higherPrice) {
        // Prices are stored in the database as integers with a value 100 times the original price
        // (e.g. £9.99 = 999). We convert the prices to integers that we can then compare with the
        // db stored prices.
        int lowerPriceInt = Math.round(lowerPrice * 100);
        int higherPriceInt = Math.round(higherPrice * 100);

        return createSQLQuery(barcode, name, categoryId, lowerPriceInt, higherPriceInt);
    }

    private SimpleSQLiteQuery createSQLQuery(String barcode, String name, long categoryId,
            Price lowerPrice, Price higherPrice) {
        // Prices are stored in the database as integers with a value 100 times the original price
        // (e.g. £9.99 = 999). We convert the prices to integers that we can then compare with the
        // db stored prices.
        int lowerPriceInt = (lowerPrice.getPounds() * 100) + lowerPrice.getPence();
        int higherPriceInt = (higherPrice.getPounds() * 100) + higherPrice.getPence();

        return createSQLQuery(barcode, name, categoryId, lowerPriceInt, higherPriceInt);
    }
}
