package com.floatingpanda.productlist;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.CategoryDao;
import com.floatingpanda.productlist.db.Price;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductDao;
import com.floatingpanda.productlist.db.ProductWithCategory;
import com.floatingpanda.productlist.repositories.ProductRepository;

import org.junit.runner.RunWith;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    public void getAllProductsWithCategoryWhenEmpty() throws InterruptedException {
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertTrue(productsWithCategories.isEmpty());
    }

    @Test
    public void getAllProductsWithCategoryWhenInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));
    }

    @Test
    public void getProductWithCategoryByProductId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productRepository.getProductWithCategoryByProductId(TestData.PRODUCT_1.getId()));

        assertNotNull(productWithCategory);
        assertThat(productWithCategory.getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void addProductWhenNoneInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertTrue(productsWithCategories.isEmpty());

        productRepository.addProduct(TestData.PRODUCT_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(1));
        assertThat(productsWithCategories.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void addProductWhenOtherProductsAlreadyInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_1);
        productsToAdd.add(TestData.PRODUCT_2);

        for(Product product : productsToAdd) {
            productRepository.addProduct(product);
        }

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size()));

        List<Product> productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertFalse(productsInDb.contains(TestData.PRODUCT_3));

        productRepository.addProduct(TestData.PRODUCT_3);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size() + 1));

        productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.contains(TestData.PRODUCT_3));
    }

    @Test
    public void addProductWhenItIsAlreadyInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productRepository.addProduct(TestData.PRODUCT_1);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(1));
        assertThat(productsWithCategories.get(0).getProduct(), is(TestData.PRODUCT_1));

        productRepository.addProduct(TestData.PRODUCT_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(1));
        assertThat(productsWithCategories.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void addProductsWhenNoneInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertTrue(productsWithCategories.isEmpty());

        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_1);
        productsToAdd.add(TestData.PRODUCT_2);

        productRepository.addProducts(productsToAdd.toArray(new Product[productsToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size()));

        List<Product> productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.containsAll(productsToAdd));
    }

    @Test
    public void addProductsWhenOthersAlreadyInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_1);
        productsToAdd.add(TestData.PRODUCT_2);

        productRepository.addProducts(productsToAdd.toArray(new Product[productsToAdd.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size()));

        List<Product> productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.containsAll(productsToAdd));

        List<ProductWithCategory> otherProductsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_3);
        productsToAdd.add(TestData.PRODUCT_4);

        productRepository.addProducts(productsToAdd.toArray(new Product[productsToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size() + otherProductsToAdd.size()));

        productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.containsAll(productsToAdd));
        assertTrue(productsInDb.containsAll(otherProductsToAdd));
    }

    @Test
    public void addProductsWhenTheyAreAlreadyInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_1);
        productsToAdd.add(TestData.PRODUCT_2);

        productRepository.addProducts(productsToAdd.toArray(new Product[productsToAdd.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size()));

        List<Product> productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.containsAll(productsToAdd));

        productRepository.addProducts(productsToAdd.toArray(new Product[productsToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(productsToAdd.size()));

        productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.containsAll(productsToAdd));
    }

    @Test
    public void editProduct() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productRepository.getProductWithCategoryByProductId(TestData.PRODUCT_1.getId()));

        assertNotNull(productWithCategory);
        assertThat(productWithCategory.getProduct().getId(), is(TestData.PRODUCT_1.getId()));
        assertThat(productWithCategory.getProduct().getName(), is(TestData.PRODUCT_1.getName()));
        assertThat(productWithCategory.getProduct().getBarcode(), is(TestData.PRODUCT_1.getBarcode()));
        assertThat(productWithCategory.getProduct().getPrice(), is(TestData.PRODUCT_1.getPrice()));
        assertThat(productWithCategory.getProduct().getCategoryId(), is(TestData.PRODUCT_1.getCategoryId()));
        assertThat(productWithCategory.getProduct().getNotes(), is(TestData.PRODUCT_1.getNotes()));

        String editedName = "New Name Yay";
        String editedBarcode = "New Barcode Yay";
        Price editedPrice = new Price(111, 62);
        long editedCategoryId = 2;
        String editedNotes = "New Notes Yay";

        Product editedProduct = new Product(productWithCategory.getProduct().getId(), editedBarcode,
                editedName, editedPrice, editedCategoryId, editedNotes);

        productRepository.editProduct(editedProduct);
        TimeUnit.MILLISECONDS.sleep(100);

        ProductWithCategory newProductWithCategory = LiveDataTestUtil.getValue(productRepository.getProductWithCategoryByProductId(TestData.PRODUCT_1.getId()));

        assertNotNull(newProductWithCategory);
        assertThat(newProductWithCategory.getProduct().getId(), is(TestData.PRODUCT_1.getId()));
        assertThat(newProductWithCategory.getProduct().getName(), is(not(TestData.PRODUCT_1.getName())));
        assertThat(newProductWithCategory.getProduct().getBarcode(), is(not(TestData.PRODUCT_1.getBarcode())));
        assertThat(newProductWithCategory.getProduct().getPrice(), is(not(TestData.PRODUCT_1.getPrice())));
        assertThat(newProductWithCategory.getProduct().getCategoryId(), is(not(TestData.PRODUCT_1.getCategoryId())));
        assertThat(newProductWithCategory.getProduct().getNotes(), is(not(TestData.PRODUCT_1.getNotes())));

        assertThat(newProductWithCategory.getProduct().getId(), is(editedProduct.getId()));
        assertThat(newProductWithCategory.getProduct().getName(), is(editedProduct.getName()));
        assertThat(newProductWithCategory.getProduct().getBarcode(), is(editedProduct.getBarcode()));
        assertThat(newProductWithCategory.getProduct().getPrice(), is(editedProduct.getPrice()));
        assertThat(newProductWithCategory.getProduct().getCategoryId(), is(editedProduct.getCategoryId()));
        assertThat(newProductWithCategory.getProduct().getNotes(), is(editedProduct.getNotes()));
    }

    @Test
    public void deleteProduct() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productRepository.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);
        assertThat(productWithCategory.getProduct(), is(TestData.PRODUCT_2));

        productRepository.deleteProduct(TestData.PRODUCT_2);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategory = LiveDataTestUtil.getValue(productRepository.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNull(productWithCategory);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size() - 1));

        for(ProductWithCategory productWithCategory1 : productsWithCategories) {
            assertFalse(productWithCategory1.getProduct() == TestData.PRODUCT_2);
        }
    }

    @Test
    public void deleteProducts() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        List<Product> productsToDelete = new ArrayList<>();
        productsToDelete.add(TestData.PRODUCT_2);
        productsToDelete.add(TestData.PRODUCT_3);

        List<Product> productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertTrue(productsInDb.containsAll(productsToDelete));

        productRepository.deleteProducts(productsToDelete.toArray(new Product[productsToDelete.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size() - productsToDelete.size()));

        productsInDb = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productsWithCategories) {
            productsInDb.add(productWithCategory.getProduct());
        }

        assertFalse(productsInDb.containsAll(productsToDelete));
    }

    @Test
    public void deleteAllProducts() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        productRepository.deleteAllProducts();
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productRepository.getAllProductsWithCategory());

        assertTrue(productsWithCategories.isEmpty());
    }

    @Test
    public void getProductsWithCategoryByExactBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        String barcode = TestData.PRODUCT_2.getBarcode();
        // Product 2 has the same barcode as product 5, so this should return 2 results.
        int listSize = 2;

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getProductsWithCategoryByExactBarcode(barcode));

        assertThat(productsWithCategories.size(), is(2));

        for(ProductWithCategory productWithCategory : productsWithCategories) {
            assertTrue(productWithCategory.getProduct().getBarcode().equals(barcode));
        }
    }

    @Test
    public void getProductsWithCategoryByCategoryId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        long categoryId = TestData.PRODUCT_1.getCategoryId();
        // Product 1 has the same categoryId as product 2, so this should return 2 results.
        int listSize = 2;

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productRepository.getProductsWithCategoryByCategoryId(categoryId));

        assertThat(productsWithCategories.size(), is(2));

        for(ProductWithCategory productWithCategory : productsWithCategories) {
            assertTrue(productWithCategory.getProduct().getCategoryId() == categoryId);
        }
    }

    // Search with partial name
    // - Partial name at start of product
    // - Partial name in mid of product
    // - Partial name at end of product
    @Test
    public void searchProductsWithCategoriesByPartialName() throws InterruptedException {
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

        // Each search should return a single result
        int listSize = 1;

        // Search with the partial name from start of the product name
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, startPartialName, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        // Search with the partial name from middle of the product name
        productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, midPartialName, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        // Search with the partial name from start of the product name
        productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, endPartialName, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));
    }

    // Search with exact name
    @Test
    public void searchProductsWithCategoriesByExactName() throws InterruptedException {
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

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, exactName, categoryId, lowerPrice, higherPrice));

        // This search should return a single result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));
    }

    // Search with partial barcode
    @Test
    public void searchProductsWithCategoriesByPartialBarcode() throws InterruptedException {
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

        // Each search should return a single result.
        int listSize = 1;

        // Search with the partial barcode from start of the product barcode
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(startPartialBarcode, name, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));

        // Search with the partial barcode from middle of the product barcode
        productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(midPartialBarcode, name, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));

        // Search with the partial barcode from end of the product barcode
        productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(endPartialBarcode, name, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));
    }

    // Search with exact barcode
    @Test
    public void searchProductsWithCategoriesByExactBarcode() throws InterruptedException {
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

        // This barcode should return a single result.
        int listSize = 1;

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));
    }

    // Search with category id
    @Test
    public void searchProductsWithCategoriesByCategoryId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        long categoryId = TestData.PRODUCT_3.getCategoryId();

        String name = null;
        String barcode = null;
        // A lower and higher price of 0 doesn't filter search results by price
        float lowerPrice = 0;
        float higherPrice = 0;

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

        // Only product 3 should have the category id associated with product 3, so searching should return 1 result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        categoryId = TestData.PRODUCT_1.getCategoryId();

        productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

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

    // Search with lower price
    @Test
    public void searchProductsWithCategoriesByLowerPrice() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's price is low enough that the search should return 3 results - products 2, 3 and 5.
        Price lowerPrice = TestData.PRODUCT_2.getPrice();

        String barcode = null;
        String name = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A higher price of 0 means the filter won't have an upper limit
        Price higherPrice = new Price(0, 0);

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

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

    // Search with higher price
    @Test
    public void searchProductsWithCategoriesByHigherPrice() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's price is high enough that the search should return 2 results - products 1 and 4.
        Price higherPrice = TestData.PRODUCT_1.getPrice();

        String barcode = null;
        String name = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;
        // A lower price of 0 means the filter won't have a lower limit
        Price lowerPrice = new Price(0, 0);

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

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

    // Search with lower price and higher price which are different
    @Test
    public void searchProductsWithCategoriesByLowerAndHigherPriceWhichAreDifferent() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // These lower and higher prices should return all 4 products in the search.
        Price lowerPrice = TestData.PRODUCT_4.getPrice();
        Price higherPrice = TestData.PRODUCT_3.getPrice();

        String barcode = null;
        String name = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

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

    // Search with lower price and higher price which are the same
    @Test
    public void searchProductsWithCategoriesByLowerAndHigherPriceWhichAreTheSame() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // These prices should result in just product 1 being returned
        Price lowerPrice = TestData.PRODUCT_1.getPrice();
        Price higherPrice = TestData.PRODUCT_1.getPrice();

        String barcode = null;
        String name = null;
        // A categoryId of 0 doesn't filter search results by category id
        long categoryId = 0;

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

        // Product 1's price as both lower and higher price should return product 1 as the only result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearch() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchUsingIntPrices() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        int lowerPrice = 549;
        int higherPrice = 2027;

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchUsingPriceClassPrices() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        Price lowerPrice = new Price(5, 49);
        Price higherPrice = new Price(20, 27);

        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(
                productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice));

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));
    }
}
