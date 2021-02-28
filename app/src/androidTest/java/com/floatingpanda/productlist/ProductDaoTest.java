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

        String newBarcode = "41244123132";
        String newName = "Newwwwww Nameeeee";
        Price newPrice = new Price(6, 79);
        long newCategoryId = 5;
        String newNotes = "New Notes";

        Product newProduct = new Product(TestData.PRODUCT_1.getId(), newBarcode, newName, newPrice, newCategoryId, newNotes);

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

        String newName1 = "New Name 1";
        String newBarcode1 = "4012441845415";
        Price newPrice1 = new Price(6, 41);
        long newCategoryId1 = 8;
        String newNotes1 = "New Notes 1";

        Product newProduct1 = new Product(TestData.PRODUCT_1.getId(), newBarcode1, newName1, newPrice1, newCategoryId1, newNotes1);

        String newName2 = "New Name 2";
        String newBarcode2 = "5145481442104";
        Price newPrice2 = new Price(1, 46);
        long newCategoryId2 = 10;
        String newNotes2 = "New Notes 2";

        Product newProduct2 = new Product(TestData.PRODUCT_2.getId(), newBarcode2, newName2, newPrice2, newCategoryId2, newNotes2);

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
    public void getAllProductsWithCategoryOrderedByBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategory =
                LiveDataTestUtil.getValue(productDao.getProductsWithCategoryOrderedByBarcode());

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

    @Test
    public void getAllProductsWithCategoryOrderedByName() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategory =
                LiveDataTestUtil.getValue(productDao.getProductsWithCategoryOrderedByName());

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

            String name = productWithCategory.getProduct().getName();
            assertTrue(previousName.compareTo(name) <= 0);

            previousName = name;
        }
    }

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

    //TODO update all these tests to use the general purpose search function. These searches are
    // obsolete and will be removed.

    // TESTS OF GENERAL PURPOSE SEARCH FUNCTION //

    /*
    @Test
    public void getProductsWithCategoryByPartialBarcodeAtStart() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        String partialBarcode = TestData.PRODUCT_1.getBarcode().substring(0, 5);
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryByBarcode(partialBarcode));

        // 2 products share this partial barcode
        int partialBarcodeListSize = 2;
        assertThat(productsWithCategories.size(), is(partialBarcodeListSize));
    }

    @Test
    public void getProductsWithCategoryByCategoryId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryByCategoryId(TestData.CATEGORY_1.getId()));

        // 2 products have this size
        int listSize = 2;
        assertThat(productsWithCategories.size(), is(listSize));
    }

    @Test
    public void getProductsWithCategoryByFullName() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryContainingName(TestData.PRODUCT_1.getName()));

        assertThat(productsWithCategories.size(), is(1));
    }

    @Test
    public void getProductsWithCategoryByPartialNameAtStartOfName() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        String nameSubstring = TestData.PRODUCT_3.getName().substring(0, 2);
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryContainingName(nameSubstring));

        assertThat(productsWithCategories.size(), is(1));
    }

    @Test
    public void getProductsWithCategoryByPartialNameInMiddle() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        String nameSubstring = TestData.PRODUCT_3.getName().substring(2, 5);
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryContainingName(nameSubstring));

        assertThat(productsWithCategories.size(), is(1));
    }

    @Test
    public void getProductsWithCategoryByPartialNameAtEnd() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        int endIndex = TestData.PRODUCT_3.getName().length();
        String nameSubstring = TestData.PRODUCT_3.getName().substring(endIndex - 3, endIndex - 1);
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryContainingName(nameSubstring));

        assertThat(productsWithCategories.size(), is(1));
    }

    @Test
    public void getProductsWithCategoryBetweenTwoPrices() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        float lowerPrice = TestData.PRODUCT_1.getPrice();
        float higherPrice = TestData.PRODUCT_2.getPrice();

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryBetweenTwoPrices(lowerPrice, higherPrice));

        int listSize = 2;
        assertThat(productsWithCategories.size(), is(listSize));
    }

    // This test is to find products with a specific price, as opposed to a range of prices.
    @Test
    public void getProductsWithCategoryBetweenTwoPricesThatAreTheSame() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        float price = TestData.PRODUCT_4.getPrice();

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryBetweenTwoPrices(price, price));

        int listSize = 1;
        assertThat(productsWithCategories.size(), is(listSize));
    }

    @Test
    public void getProductsWithCategoryByCategoryIdAndContainingName() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        long categoryId = TestData.PRODUCT_1.getCategoryId();
        String name = TestData.PRODUCT_1.getName().substring(0, 2);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryByCategoryIdAndContainingName(categoryId, name));

        int listSize = 1;
        assertThat(productsWithCategories.size(), is(listSize));
    }

    @Test
    public void getProductsWithCategoryByCategoryIdAndBetweenTwoPrices() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        long categoryId = TestData.PRODUCT_1.getCategoryId();
        float lowerPrice = TestData.PRODUCT_1.getPrice();
        float higherPrice = TestData.PRODUCT_2.getPrice();

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryByCategoryIdAndBetweenTwoPrices(categoryId, lowerPrice, higherPrice));

        int listSize = 2;
        assertThat(productsWithCategories.size(), is(listSize));
    }

    @Test
    public void getProductsWithCategoryContainingNameAndBetweenTwoPrices() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        String name = TestData.PRODUCT_1.getName().substring(0, 2);
        float price = TestData.PRODUCT_1.getPrice();

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryContainingNameAndBetweenTwoPrices(name, price, price));

        int listSize = 1;
        assertThat(productsWithCategories.size(), is(listSize));
    }

    @Test
    public void getProductsWithCategoryByCategoryIdAndContainingNameAndBetweenPrices() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        long categoryId = TestData.PRODUCT_1.getCategoryId();
        String name = TestData.PRODUCT_1.getName().substring(0, 2);
        float price = TestData.PRODUCT_1.getPrice();

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(
                productDao.getProductsWithCategoryByCategoryIdAndContainingNameAndBetweenPrices(
                        categoryId, name, price, price));

        int listSize = 1;
        assertThat(productsWithCategories.size(), is(listSize));
    }
    */

    @Test
    public void searchProductsWithCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        SimpleSQLiteQuery query = getQuery(barcode, name, categoryId, lowerPrice, higherPrice);
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

    private SimpleSQLiteQuery getQuery(String barcode, String name, long categoryId, float lowerPrice, float higherPrice) {
        String queryString = "SELECT * FROM products";

        List<Object> args = new ArrayList<>();

        // Tracks whether already using a WHERE clause in the query
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

        if (lowerPrice >= 0f && higherPrice >= lowerPrice) {
            if (!whereStarted) {
                queryString += " WHERE";
                whereStarted = true;
            } else {
                queryString += " AND";
            }

            queryString += " price >= ?";

            int lowerPriceInt = Math.round(lowerPrice * 100);
            args.add(lowerPriceInt);

            Log.w("test", "lowerPriceInt = " + lowerPriceInt);

            queryString += " AND price <= ?";

            int higherPriceInt = Math.round(higherPrice * 100);
            args.add(higherPriceInt);

            Log.w("test", "higherPriceInt = " + higherPriceInt);
        }

        return new SimpleSQLiteQuery(queryString, args.toArray());
    }
}
