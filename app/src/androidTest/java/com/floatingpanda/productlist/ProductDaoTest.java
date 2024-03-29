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
import com.floatingpanda.productlist.db.Price;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductDao;
import com.floatingpanda.productlist.db.ProductWithCategory;
import com.floatingpanda.productlist.other.OrderByEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ProductDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;

    private CategoryDao categoryDao;
    private ProductDao productDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
        productDao = db.productDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void getAllProductsWhenNoneInserted() throws InterruptedException {
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertTrue(products.isEmpty());
    }

    @Test
    public void getAllProductsWhenAllInserted() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));
    }

    @Test
    public void getProductByIdWhenNoneInserted() throws InterruptedException {
        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_1.getId()));

        assertNull(product);
    }

    @Test
    public void getProductByIdWhenAllInserted() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_1.getId()));

        assertThat(product, is(TestData.PRODUCT_1));
    }

    @Test
    public void insertProductWhenNoneInserted() throws InterruptedException {
        productDao.insert(TestData.PRODUCT_1);
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));
        assertThat(products.get(0), is(TestData.PRODUCT_1));
        assertThat(products.get(0).getId(), is(TestData.PRODUCT_1.getId()));
    }

    @Test
    public void insertProductWhenAnotherProductAlreadyInserted() throws InterruptedException {
        productDao.insert(TestData.PRODUCT_1);
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));

        productDao.insert(TestData.PRODUCT_2);
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(2));
    }

    @Test
    public void insertProductWhenItWasAlreadyInserted() throws InterruptedException {
        productDao.insert(TestData.PRODUCT_1);
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));

        productDao.insert(TestData.PRODUCT_1);
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));
    }

    @Test
    public void insertMultipleProductsWhenNoneInserted() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));

        for (Product product : products) {
            assertTrue(TestData.PRODUCTS.contains(product));
        }
    }

    @Test
    public void insertMultipleProductsWhenAnotherProductAlreadyInserted() throws InterruptedException {
        productDao.insert(TestData.PRODUCT_1);
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));

        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_2);
        productsToAdd.add(TestData.PRODUCT_3);

        productDao.insertMultiple(productsToAdd.toArray(new Product[productsToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        List<Product> newProducts = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(newProducts.size(), is(products.size() + productsToAdd.size()));
    }

    @Test
    public void insertMultipleProductsWhenTheyWereAlreadyInserted() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));

        for (Product product : products) {
            assertTrue(TestData.PRODUCTS.contains(product));
        }

        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));

        for (Product product : products) {
            assertTrue(TestData.PRODUCTS.contains(product));
        }
    }

    @Test
    public void updateProduct() throws InterruptedException {
        productDao.insert(TestData.PRODUCT_1);
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));
        assertThat(products.get(0), is(TestData.PRODUCT_1));
        assertThat(products.get(0).getId(), is(TestData.PRODUCT_1.getId()));

        String newName = "Newwwwww Nameeeee";
        String newBarcode = "41244123132";
        Price newPrice = new Price(6, 79);
        long newCategoryId = 5;
        String newNotes = "New Notes";

        Product newProduct = new Product(TestData.PRODUCT_1.getId(), newName, newBarcode, newPrice, newCategoryId, newNotes);

        productDao.update(newProduct);
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(1));
        assertThat(products.get(0), is(not(TestData.PRODUCT_1)));
        assertThat(products.get(0), is(newProduct));
        assertThat(products.get(0).getId(), is(TestData.PRODUCT_1.getId()));
        assertThat(products.get(0).getId(), is(newProduct.getId()));
    }

    @Test
    public void updateMultipleProducts() throws InterruptedException {
        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_1);
        productsToAdd.add(TestData.PRODUCT_2);

        productDao.insertMultiple(productsToAdd.toArray(new Product[productsToAdd.size()]));

        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(productsToAdd.size()));

        for (Product product : products) {
            assertTrue(TestData.PRODUCTS.contains(product));
        }

        String newBarcode1 = "4012441845415";
        String newName1 = "New Name 1";
        Price newPrice1 = new Price(6, 41);
        long newCategoryId1 = 8;
        String newNotes1 = "New Notes 1";

        Product newProduct1 = new Product(TestData.PRODUCT_1.getId(), newName1, newBarcode1, newPrice1, newCategoryId1, newNotes1);

        String newBarcode2 = "5145481442104";
        String newName2 = "New Name 2";
        Price newPrice2 = new Price(1, 46);
        long newCategoryId2 = 10;
        String newNotes2 = "New Notes 2";

        Product newProduct2 = new Product(TestData.PRODUCT_2.getId(), newName2, newBarcode2, newPrice2, newCategoryId2, newNotes2);

        List<Product> updatedProducts = new ArrayList<>();
        updatedProducts.add(newProduct1);
        updatedProducts.add(newProduct2);

        productDao.updateMultiple(updatedProducts.toArray(new Product[updatedProducts.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(productsToAdd.size()));

        for (Product product : products) {
            assertTrue(updatedProducts.contains(product));
            assertFalse(TestData.PRODUCTS.contains(product));
        }
    }

    @Test
    public void deleteProduct() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));
        assertTrue(products.contains(TestData.PRODUCT_1));

        productDao.delete(TestData.PRODUCT_1);
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size() - 1));
        assertFalse(products.contains(TestData.PRODUCT_1));
    }

    @Test
    public void deleteMultipleProducts() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));
        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_2));

        List<Product> productsToDelete = new ArrayList<>();
        productsToDelete.add(TestData.PRODUCT_1);
        productsToDelete.add(TestData.PRODUCT_2);

        productDao.deleteMultiple(productsToDelete.toArray(new Product[productsToDelete.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size() - productsToDelete.size()));
        assertFalse(products.contains(TestData.PRODUCT_1));
        assertFalse(products.contains(TestData.PRODUCT_2));
    }

    @Test
    public void deleteAllProducts() throws InterruptedException {
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));
        List<Product> products = LiveDataTestUtil.getValue(productDao.getAll());

        assertThat(products.size(), is(TestData.PRODUCTS.size()));

        productDao.deleteAll();
        TimeUnit.MILLISECONDS.sleep(100);

        products = LiveDataTestUtil.getValue(productDao.getAll());

        assertTrue(products.isEmpty());
    }

    @Test
    public void getAllProductsWithCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategory =
                LiveDataTestUtil.getValue(productDao.getProductsWithCategory());

        assertThat(productsWithCategory.size(), is(TestData.PRODUCTS.size()));

        String previousName = "";
        for (ProductWithCategory productWithCategory : productsWithCategory) {
            if (productWithCategory.getCategory() == null) {
                continue;
            }

            assertThat(productWithCategory.getProduct().getCategoryId(), is(productWithCategory.getCategory().getId()));

            for (Category category : TestData.CATEGORIES) {
                if (productWithCategory.getCategory().getId() == category.getId()) {
                    assertTrue(productWithCategory.getCategory().getName().equals(category.getName()));
                }
            }

            /*
            String name = productWithCategory.getProduct().getName();
            assertTrue(previousName.compareTo(name) <= 0);

            previousName = name;
             */
        }
    }

    /*
    @Test
    public void getAllProductsWithCategoryOrderedByBarcodeAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategory =
                LiveDataTestUtil.getValue(productDao.getProductsWithCategoryOrderedByBarcodeAsc());

        assertThat(productsWithCategory.size(), is(TestData.PRODUCTS.size()));

        String previousBarcode = "0000000000000";
        for (ProductWithCategory productWithCategory : productsWithCategory) {
            if (productWithCategory.getCategory() == null) {
                continue;
            }

            assertThat(productWithCategory.getProduct().getCategoryId(), is(productWithCategory.getCategory().getId()));

            for (Category category : TestData.CATEGORIES) {
                if (productWithCategory.getCategory().getId() == category.getId()) {
                    assertTrue(productWithCategory.getCategory().getName().equals(category.getName()));
                }
            }

            String barcode = productWithCategory.getProduct().getBarcode();
            assertTrue(previousBarcode.compareTo(barcode) <= 0);

            previousBarcode = barcode;
        }
    }

     */

    @Test
    public void getProductWithCategoryByProductId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(
                productDao.getProductWithCategoryByProductId(TestData.PRODUCT_1.getId()));

        assertThat(productWithCategory.getProduct(), is(TestData.PRODUCT_1));
        assertThat(productWithCategory.getCategory().getId(), is(TestData.PRODUCT_1.getCategoryId()));
    }

    @Test
    public void getProductWithCategoryByProductIdWhenProductHasNoCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(
                productDao.getProductWithCategoryByProductId(TestData.PRODUCT_4.getId()));

        assertThat(productWithCategory.getProduct(), is(TestData.PRODUCT_4));
        assertNull(productWithCategory.getCategory());
    }

    @Test
    public void getProductsWithCategoryByExactBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryByExactBarcode(TestData.PRODUCT_1.getBarcode()));

        // Only 1 product with this barcode
        int fullBarcodeListSize = 1;
        assertThat(productsWithCategories.size(), is(fullBarcodeListSize));
    }

    // TESTS OF GENERAL PURPOSE SEARCH FUNCTION //

    @Test
    public void searchProductsWithCategoryByPartialName() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String fullName = TestData.PRODUCT_3.getName();
        // First 3 letters of product name
        String startPartialName = fullName.substring(0, 2);

        // Middle 3 letters of product name
        String midPartialName = fullName.substring(2, 4);

        // End 3 letters of product name
        String endPartialName = fullName.substring(fullName.length() - 3, fullName.length() - 1);

        String barcode = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A lower and higher price of 0 doesn't filter search results by price
        float lowerPrice = 0;
        float higherPrice = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        // Query to search with the partial name from start of the product name
        SimpleSQLiteQuery startQuery = createQuery(startPartialName, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(startQuery));

        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        // Query to search with partial name from middle of the product name
        SimpleSQLiteQuery midQuery = createQuery(midPartialName, barcode, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productDao.searchProductsWithCategory(midQuery));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        // Query to search with partial name from end of the product name
        SimpleSQLiteQuery endQuery = createQuery(endPartialName, barcode, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productDao.searchProductsWithCategory(endQuery));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));
    }

    @Test
    public void searchProductsWithCategoryByExactName() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String exactName = TestData.PRODUCT_3.getName();

        String barcode = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A lower and higher price of 0 doesn't filter search results by price
        float lowerPrice = 0;
        float higherPrice = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(exactName, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));
    }

    @Test
    public void searchProductsWithCategoryByPartialBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        // PRODUCT 4 used because it has unique barcode
        String exactBarcode = TestData.PRODUCT_4.getBarcode();

        // First 4 numbers of barcode - 0987
        String startPartialBarcode = exactBarcode.substring(0, 3);
        // Middle 4 numbers of barcode - 6123
        String midPartialBarcode = exactBarcode.substring(4, 7);
        // End 4 numbers of barcode - 5875
        String endPartialBarcode = exactBarcode.substring(exactBarcode.length() - 5, exactBarcode.length() - 1);

        String name = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A lower and higher price of 0 doesn't filter search results by price
        float lowerPrice = 0;
        float higherPrice = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        // Query to search with the partial barcode from start of the product barcode
        SimpleSQLiteQuery startQuery = createQuery(name, startPartialBarcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(startQuery));

        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));

        // Query to search with the partial barcode from middle of the product barcode
        SimpleSQLiteQuery midQuery = createQuery(name, midPartialBarcode, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productDao.searchProductsWithCategory(midQuery));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));

        // Query to search with the partial barcode from end of the product barcode
        SimpleSQLiteQuery endQuery = createQuery(name, endPartialBarcode, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productDao.searchProductsWithCategory(endQuery));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));
    }

    @Test
    public void searchProductsWithCategoryByGeneralSearchWithExactBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        String barcode = TestData.PRODUCT_4.getBarcode();

        String name = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A lower and higher price of 0 doesn't filter search results by price
        float lowerPrice = 0;
        float higherPrice = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));
    }

    @Test
    public void searchProductsWithCategoryByCategoryId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        long categoryId = TestData.PRODUCT_3.getCategoryId();

        String name = null;
        String barcode = null;
        // A lower and higher price of 0 doesn't filter search results by price
        float lowerPrice = 0;
        float higherPrice = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Only product 3 should have the category id associated with product 3, so searching should return 1 result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        categoryId = TestData.PRODUCT_1.getCategoryId();

        query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productDao.searchProductsWithCategory(query));

        // Product 1's category id is shared with product 2, so searching should return 2 results.
        listSize = 2;

        assertThat(productsWithCategory.size(), is(listSize));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory : productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_2));
    }

    @Test
    public void searchProductsWithCategoryByLowerPrice() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's price is low enough that the search should return 3 results - products 2, 3 and 5.
        Price lowerPrice = TestData.PRODUCT_2.getPrice();

        String name = null;
        String barcode = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A higher price of 0 means the filter won't have an upper limit
        Price higherPrice = new Price(0, 0);
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Product 2's price should result in 3 results in the list.
        int listSize = 3;

        assertThat(productsWithCategory.size(), is(listSize));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory : productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.contains(TestData.PRODUCT_2));
        assertTrue(products.contains(TestData.PRODUCT_3));
    }

    @Test
    public void searchProductsWithCategoryByHigherPrice() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's price is high enough that the search should return 2 results - products 1 and 4.
        Price higherPrice = TestData.PRODUCT_1.getPrice();

        String name = null;
        String barcode = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A lower price of 0 means the filter won't have a lower limit
        Price lowerPrice = new Price(0, 0);
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Product 1's price should result in 2 results in the list.
        int listSize = 2;

        assertThat(productsWithCategory.size(), is(listSize));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory : productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_4));
    }

    @Test
    public void searchProductsWithCategoryByDifferentLowerPriceAndHigherPrice() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // These lower and higher prices should return all 4 products in the search.
        Price lowerPrice = TestData.PRODUCT_4.getPrice();
        Price higherPrice = TestData.PRODUCT_3.getPrice();

        String name = null;
        String barcode = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Product 4's price as lower price and product 1's price as higher price should result in
        // all 4 products being in the returned list.
        int listSize = 4;

        assertThat(productsWithCategory.size(), is(listSize));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory : productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_2));
        assertTrue(products.contains(TestData.PRODUCT_3));
        assertTrue(products.contains(TestData.PRODUCT_4));
    }

    @Test
    public void searchProductsWithCategoryBySameLowerPriceAndHigherPrice() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // These prices should result in just product 1 being returned
        Price lowerPrice = TestData.PRODUCT_1.getPrice();
        Price higherPrice = TestData.PRODUCT_1.getPrice();

        String name = null;
        String barcode = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Product 1's price as both lower and higher price should return product 1 as the only result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void searchProductsWithCategoryBySameLowerPriceAndHigherPriceWithEmptyStringNameAndBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // These prices should result in just product 1 being returned
        Price lowerPrice = TestData.PRODUCT_1.getPrice();
        Price higherPrice = TestData.PRODUCT_1.getPrice();

        String name = "";
        String barcode = "";
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Product 1's price as both lower and higher price should return product 1 as the only result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void searchProductsWithCategoryBySameLowerPriceAndHigherPriceWithWhiteSpaceNameAndBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // These prices should result in just product 1 being returned
        Price lowerPrice = TestData.PRODUCT_1.getPrice();
        Price higherPrice = TestData.PRODUCT_1.getPrice();

        String name = "             ";
        String barcode = "                              ";
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productDao.searchProductsWithCategory(query));

        // Product 1's price as both lower and higher price should return product 1 as the only result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    //TODO write search tests combining test cases from above
    //name and barcode
    //name and category id
    //name and lower price
    //name and higher price
    //name and lower price and higher price
    //barcode and category id
    //barcode and lower price
    //barcode and higher price
    //category id and lower price
    //category id and higher price
    //category id and lower price and higher price
    //name and barcode and category id
    //name and barcode and lower price
    //name and barcode and higher price
    //name and barcode and lower price and higher price
    //name and category id and lower price
    //name and category id and higher price
    //name and category id and lower price and higher price
    //barcode and category id and lower price
    //barcode and category id and higher price
    //barcode and category id and lower price and higher price
    //name and barcode and category id and lower price
    //name and barcode and category id and lower price and higher price

    @Test
    public void searchProductsWithCategoryByRealisticSearch() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String name = "p";
        String barcode = "12345";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;
        OrderByEnum orderBy = OrderByEnum.NAME_ASC;

        SimpleSQLiteQuery query = createQuery(name, barcode, categoryId, lowerPrice, higherPrice);
        Log.w("ProductDaoTest", "Query made: " + query.getSql());
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productDao.searchProductsWithCategory(query));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            Log.w("ProductDaoTest", "Product: " + productWithCategory.getProduct().getName());
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));
    }

    //TODO write simple tests for different order by settings
    //Search ordered by name DESC
    @Test
    public void searchProductsWithCategoryOrderedByNameDesc() throws InterruptedException {
        // Do search which gets all products as results
        // Test that they are ordered properly
    }

    //Search ordered by barcode ASC
    @Test
    public void searchProductsWithCategoryOrderedByBarcodeAsc() throws InterruptedException {

    }

    //Search ordered by barcode DESC
    @Test
    public void searchProductsWithCategoryOrderedByBarcodeDesc() throws InterruptedException {

    }

    //Search ordered by price ASC
    @Test
    public void searchProductsWithCategoryOrderedByPriceAsc() throws InterruptedException {

    }

    //Search ordered by price DESC
    @Test
    public void searchProductsWithCategoryOrderedByPriceDesc() throws InterruptedException {

    }

    private SimpleSQLiteQuery createQuery(String name, String barcode, long categoryId, int lowerPrice, int higherPrice) {
        String queryString = "SELECT * FROM products";

        List<Object> args = new ArrayList<>();

        // Tracks whether already using a WHERE clause in the query
        boolean whereStarted = false;

        if (name != null && !name.trim().isEmpty()) {
            queryString += " WHERE Upper(name) LIKE '%' || Upper(?) || '%'";
            whereStarted = true;
            args.add(name);
        }

        if (barcode != null && !barcode.trim().isEmpty()) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " barcode LIKE '%' || ? || '%'";
            args.add(barcode);
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

        if (lowerPrice > 0) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " price >= ?";

            args.add(lowerPrice);
        }

        if (higherPrice > 0) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " price <= ?";

            args.add(higherPrice);
        }

        Log.w("Test", "Query: " + queryString);
        return new SimpleSQLiteQuery(queryString, args.toArray());
    }

    /*
    private String createOrderByString(OrderByEnum orderBy) {
        String queryString = "";
        switch (orderBy) {
            case NO_ORDER:
                break;
            case BARCODE:
                queryString += " ORDER BY barcode";
                break;
            case BARCODE_INVERTED:
                queryString += " ORDER BY barcode DESC";
                break;
            case PRICE:
                queryString += " ORDER BY price";
                break;
            case PRICE_INVERTED:
                queryString += " ORDER BY price DESC";
                break;
            case NAME_INVERTED:
                queryString += " ORDER BY name DESC";
                break;
            case NAME:
                queryString += " ORDER BY name";
                break;
            default:
                // Exception is used in default instead of ordering by name to improve maintainability
                // for the future. Now if someone adds an ENUM but doesn't add it to the switch, they
                // will find out quickly through this exception.
                throw new IllegalStateException("Invalid ENUM was entered.");
        }

        return queryString;
    }

     */

    private SimpleSQLiteQuery createQuery(String name, String barcode, long categoryId, float lowerPrice, float higherPrice) {
        // Prices are stored in the database as integers with a value 100 times the original price
        // (e.g. £9.99 = 999). We convert the prices to integers that we can then compare with the
        // db stored prices.
        int lowerPriceInt = Math.round(lowerPrice * 100);
        int higherPriceInt = Math.round(higherPrice * 100);

        return createQuery(name, barcode, categoryId, lowerPriceInt, higherPriceInt);
    }

    private SimpleSQLiteQuery createQuery(String name, String barcode, long categoryId,
                                             Price lowerPrice, Price higherPrice) {
        // Prices are stored in the database as integers with a value 100 times the original price
        // (e.g. £9.99 = 999). We convert the prices to integers that we can then compare with the
        // db stored prices.
        int lowerPriceInt = (lowerPrice.getPounds() * 100) + lowerPrice.getPence();
        int higherPriceInt = (higherPrice.getPounds() * 100) + higherPrice.getPence();

        return createQuery(name, barcode, categoryId, lowerPriceInt, higherPriceInt);
    }
}
