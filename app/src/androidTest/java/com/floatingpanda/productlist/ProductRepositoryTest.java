package com.floatingpanda.productlist;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.CategoryDao;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductDao;
import com.floatingpanda.productlist.db.ProductWithCategory;
import com.floatingpanda.productlist.repositories.CategoryRepository;
import com.floatingpanda.productlist.repositories.ProductRepository;

import org.junit.runner.RunWith;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProductRepositoryTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;

    private CategoryDao categoryDao;
    private ProductDao productDao;
    private ProductRepository productRepository;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
        productDao = db.productDao();
        productRepository = new ProductRepository(db);
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void getAllProductsWithCategoryOrderedByNameAscWhenEmpty() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByNameAscWhenInserted() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByNameDesc() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByBarcodeAscWhenEmpty() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByBarcodeAscWhenInserted() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByBarcodeDesc() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByPriceAscWhenEmpty() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByPriceAscWhenInserted() throws InterruptedException {

    }

    @Test
    public void getAllProductsWithCategoryOrderedByPriceDesc() throws InterruptedException {

    }

    @Test
    public void getProductWithCategoryByProductId(long productId) {

    }

    @Test
    public void addProductWhenNoneInserted() throws InterruptedException {

    }

    @Test
    public void addProductWhenOtherProductsAlreadyInserted() throws InterruptedException {

    }

    @Test
    public void addProductWhenItIsAlreadyInserted() throws InterruptedException {

    }

    @Test
    public void addProductsWhenNoneInserted() throws InterruptedException {

    }

    @Test
    public void addProductsWhenOthersAlreadyInserted() throws InterruptedException {

    }

    @Test
    public void addProductsWhenTheyAreAlreadyInserted() throws InterruptedException {

    }

    @Test
    public void editProduct() throws InterruptedException {

    }

    @Test
    public void deleteProduct() throws InterruptedException {

    }

    @Test
    public void deleteProducts() throws InterruptedException {

    }

    @Test
    public void deleteAllProducts() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByExactBarcodeOrderedByNameAsc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByExactBarcodeOrderedByNameDesc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByExactBarcodeOrderedByPriceAsc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByExactBarcodeOrderedByPriceDesc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByCategoryIdOrderedByNameAsc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByCategoryIdOrderedByNameDesc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByCategoryIdOrderedByBarcodeAsc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByCategoryIdOrderedByBarcodeDesc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByCategoryIdOrderedByPriceAsc() throws InterruptedException {

    }

    @Test
    public void getProductsWithCategoryByCategoryIdOrderedByPriceDesc() throws InterruptedException {

    }

    // Search with partial name
    // - Partial name at start of product
    // - Partial name in mid of product
    // - Partial name at end of product
    @Test
    public void searchProductsWithCategoriesByPartialName() throws InterruptedException {

    }

    // Search with exact name
    @Test
    public void searchProductsWithCategoriesByExactName() throws InterruptedException {

    }

    // Search with partial barcode
    @Test
    public void searchProductsWithCategoriesByPartialBarcode() throws InterruptedException {

    }

    // Search with exact barcode
    @Test
    public void searchProductsWithCategoriesByExactBarcode() throws InterruptedException {

    }

    // Search with category id
    @Test
    public void searchProductsWithCategoriesByCategoryId() throws InterruptedException {

    }

    // Search with lower price
    @Test
    public void searchProductsWithCategoriesByLowerPrice() throws InterruptedException {

    }

    // Search with higher price
    @Test
    public void searchProductsWithCategoriesByHigherPrice() throws InterruptedException {

    }

    // Search with lower price and higher price which are the same
    @Test
    public void searchProductsWithCategoriesByLowerAndHigherPriceWhichAreTheSame() throws InterruptedException {

    }

    // Search with lower price and higher price which are different
    @Test
    public void searchProductsWithCategoriesByLowerAndHigherPriceWhichAreDifferent() throws InterruptedException {

    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearch() throws InterruptedException {

    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchUsingFloatPrices() throws InterruptedException {

    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchUsingPriceClassPrices() throws InterruptedException {

    }

    // Search ordered by name DESC
    @Test
    public void searchProductsWithCategoryOrderedByNameDesc() throws InterruptedException {
        // Do search which gets all products as results
        // Test that they are ordered properly
    }

    // Search ordered by barcode ASC
    @Test
    public void searchProductsWithCategoryOrderedByBarcodeAsc() throws InterruptedException {

    }

    // Search ordered by barcode DESC
    @Test
    public void searchProductsWithCategoryOrderedByBarcodeDesc() throws InterruptedException {

    }

    // Search ordered by price ASC
    @Test
    public void searchProductsWithCategoryOrderedByPriceAsc() throws InterruptedException {

    }

    // Search ordered by price DESC
    @Test
    public void searchProductsWithCategoryOrderedByPriceDesc() throws InterruptedException {

    }

    /*
    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, float lowerPrice, float higherPrice, OrderByEnum orderBy) {
        SimpleSQLiteQuery query = createSQLQuery(barcode, name, categoryId, lowerPrice, higherPrice, orderBy);
        return productDao.searchProductsWithCategory(query);
    }

    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, int lowerPrice, int higherPrice, OrderByEnum orderBy) {
        SimpleSQLiteQuery query = createSQLQuery(barcode, name, categoryId, lowerPrice, higherPrice, orderBy);
        return productDao.searchProductsWithCategory(query);
    }

    //TODO write tests which use different search types and orderings
    public LiveData<List<ProductWithCategory>> searchProductsWithCategory(String barcode, String name,
            long categoryId, Price lowerPrice, Price higherPrice, OrderByEnum orderBy) {
        SimpleSQLiteQuery query = createSQLQuery(barcode, name, categoryId, lowerPrice, higherPrice, orderBy);
        return productDao.searchProductsWithCategory(query);
    }
     */
}
