package com.floatingpanda.productlist;

import android.content.Context;
import android.provider.ContactsContract;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Query;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.CategoryDao;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductDao;
import com.floatingpanda.productlist.db.ProductWithCategory;

import static org.hamcrest.Matchers.both;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
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
        float newPrice = 6.79f;
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
        float newPrice1 = 6.41f;
        long newCategoryId1 = 8;
        String newNotes1 = "New Notes 1";

        Product newProduct1 = new Product(TestData.PRODUCT_1.getId(), newBarcode1, newName1, newPrice1, newCategoryId1, newNotes1);

        String newName2 = "New Name 2";
        String newBarcode2 = "5145481442104";
        float newPrice2 = 1.46f;
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
}