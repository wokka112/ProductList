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
import com.floatingpanda.productlist.other.OrderByEnum;
import com.floatingpanda.productlist.ui.products.ProductViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ProductViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;

    private CategoryDao categoryDao;
    private ProductDao productDao;
    private ProductViewModel productViewModel;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
        productDao = db.productDao();
        productViewModel = new ProductViewModel(ApplicationProvider.getApplicationContext(), db);
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void getNaturalOrderingAndCurrentOrderingEnumsAfterCreationAndRepopulatingListAndSearchingDatabase() throws InterruptedException {
        // Natural ordering is always alphabetically by name.
        OrderByEnum naturalOrdering = productViewModel.getNaturalOrdering();

        assertSame(OrderByEnum.NAME_ASC, naturalOrdering);

        // Current ordering always starts as natural ordering.
        OrderByEnum currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(naturalOrdering, currentOrdering);

        // Populates list and gets rid of null value from MediatorLiveData
        LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        // Reordering will change the currentOrdering.
        OrderByEnum newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(newOrdering, currentOrdering);

        // Repopulating the list with all products will change ordering back to the natural ordering
        productViewModel.populateListWithAllProductsWithCategories();
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(naturalOrdering, currentOrdering);

        // Searching the list by barcode will change ordering back to the natural ordering
        newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(newOrdering, currentOrdering);

        productViewModel.searchProductsWithCategoryByBarcode(TestData.PRODUCT_1.getBarcode());
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(naturalOrdering, currentOrdering);

        // Filtering the list by category id will change ordering back to the natural ordering
        newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(newOrdering, currentOrdering);

        productViewModel.filterProductsWithCategoryByCategoryId(TestData.PRODUCT_1.getCategoryId());
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(naturalOrdering, currentOrdering);

        // Searching the list will change ordering back to the natural ordering
        newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(newOrdering, currentOrdering);

        productViewModel.searchProductsWithCategory(TestData.PRODUCT_1.getBarcode(), null, 0, 0f, 0f);
        currentOrdering = productViewModel.getCurrentOrdering();

        assertSame(naturalOrdering, currentOrdering);
    }

    @Test
    public void testNullGuardOnReorderWhenLiveDataIsNull() {
        try {
            productViewModel.reorderProductList(OrderByEnum.NAME_DESC);
            fail("No exception produced. Null pointer exception expected.");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void getUnsortedProductsWithCategoriesWhenNoneInserted() throws InterruptedException {
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getUnsortedProductsWithCategories());

        assertTrue(productsWithCategories.isEmpty());
    }

    @Test
    public void getUnsortedProductsWithCategoriesWhenAllInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getUnsortedProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        // Unsorted list should have no ordering imposed, unlike the sorted list which has a name ordering imposed.
        // Hence we are testing that there is no ordering.
        boolean allSorted = true;
        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            if (currentName.compareTo(previousName) > 0) {
                allSorted = false;
            }
        }

        assertFalse(allSorted);
    }

    @Test
    public void getSortedProductsWithCategoriesWhenNoneInserted() throws InterruptedException {
        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertTrue(productsWithCategories.isEmpty());
    }

    @Test
    public void getSortedProductsWithCategoriesWhenAllInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void addProductFromProductWithCategoryWhenNoneInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        productViewModel.addProductFromProductWithCategory(TestData.PRODUCT_1_WITH_CATEGORY);

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(1));
        assertThat(productWithCategories.get(0), is(TestData.PRODUCT_1_WITH_CATEGORY));
    }

    @Test
    public void addProductFromProductWithCategoryWhenSomeOthersInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<ProductWithCategory> productWithCategoriesToAdd = new ArrayList<>();
        productWithCategoriesToAdd.add(TestData.PRODUCT_1_WITH_CATEGORY);
        productWithCategoriesToAdd.add(TestData.PRODUCT_2_WITH_CATEGORY);
        productWithCategoriesToAdd.add(TestData.PRODUCT_3_WITH_CATEGORY);

        for (ProductWithCategory productWithCategory : productWithCategoriesToAdd) {
            productViewModel.addProductFromProductWithCategory(productWithCategory);
        }
        TimeUnit.MILLISECONDS.sleep(100);

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(productWithCategoriesToAdd.size()));
        assertTrue(productWithCategories.containsAll(productWithCategoriesToAdd));

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void addProductFromProductWithCategoryWhenAlreadyInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        productViewModel.addProductFromProductWithCategory(TestData.PRODUCT_1_WITH_CATEGORY);

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(1));
        assertThat(productWithCategories.get(0), is(TestData.PRODUCT_1_WITH_CATEGORY));

        productViewModel.addProductFromProductWithCategory(TestData.PRODUCT_1_WITH_CATEGORY);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(1));
        assertThat(productWithCategories.get(0), is(TestData.PRODUCT_1_WITH_CATEGORY));
    }

    @Test
    public void addProductWhenNoneInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        productViewModel.addProduct(TestData.PRODUCT_1);

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(1));
        assertThat(productWithCategories.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void addProductWhenSomeOthersInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<Product> productsToAdd = new ArrayList<>();
        productsToAdd.add(TestData.PRODUCT_1);
        productsToAdd.add(TestData.PRODUCT_2);
        productsToAdd.add(TestData.PRODUCT_3);

        for (Product product : productsToAdd) {
            productViewModel.addProduct(product);
        }

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(3));

        List<Product> products = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productWithCategories) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.containsAll(productsToAdd));

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void addProductWhenAlreadyInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        productViewModel.addProduct(TestData.PRODUCT_1);

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(1));
        assertThat(productWithCategories.get(0).getProduct(), is(TestData.PRODUCT_1));

        productViewModel.addProduct(TestData.PRODUCT_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(1));
        assertThat(productWithCategories.get(0).getProduct(), is(TestData.PRODUCT_1));
    }

    @Test
    public void editProductFromProductWithCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);

        // Test products are ordered by name
        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        String newName = "New name is here";
        String newBarcode = "1234091231923";
        long newCategoryId = 3;
        Price newPrice = new Price(11, 24);
        String newNotes = "New notes are here";

        productWithCategory.getProduct().setName(newName);
        productWithCategory.getProduct().setBarcode(newBarcode);
        productWithCategory.getProduct().setCategoryId(newCategoryId);
        productWithCategory.getProduct().setPrice(newPrice);
        productWithCategory.getProduct().setNotes(newNotes);

        productViewModel.editProductFromProductWithCategory(productWithCategory);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);

        assertEquals(TestData.PRODUCT_2.getId(), productWithCategory.getProduct().getId());
        assertEquals(newName, productWithCategory.getProduct().getName());
        assertEquals(newBarcode, productWithCategory.getProduct().getBarcode());
        assertEquals(newCategoryId, productWithCategory.getProduct().getCategoryId());
        assertEquals(newPrice, productWithCategory.getProduct().getPrice());
        assertEquals(newNotes, productWithCategory.getProduct().getNotes());

        // Test products are still ordered by name
        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void editProduct() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);

        // Test products are ordered by name
        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        String newName = "New name is here";
        String newBarcode = "1234091231923";
        long newCategoryId = 3;
        Price newPrice = new Price(11, 24);
        String newNotes = "New notes are here";

        productWithCategory.getProduct().setName(newName);
        productWithCategory.getProduct().setBarcode(newBarcode);
        productWithCategory.getProduct().setCategoryId(newCategoryId);
        productWithCategory.getProduct().setPrice(newPrice);
        productWithCategory.getProduct().setNotes(newNotes);

        productViewModel.editProduct(productWithCategory.getProduct());
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);

        assertEquals(TestData.PRODUCT_2.getId(), productWithCategory.getProduct().getId());
        assertEquals(newName, productWithCategory.getProduct().getName());
        assertEquals(newBarcode, productWithCategory.getProduct().getBarcode());
        assertEquals(newCategoryId, productWithCategory.getProduct().getCategoryId());
        assertEquals(newPrice, productWithCategory.getProduct().getPrice());
        assertEquals(newNotes, productWithCategory.getProduct().getNotes());

        // Test products are still ordered by name
        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void deleteProductFromProductWithCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Test products are ordered by name
        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);
        assertThat(productWithCategory.getProduct(), is(TestData.PRODUCT_2));

        productViewModel.deleteProductFromProductWithCategory(productWithCategory);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNull(productWithCategory);

        // Test products are still ordered by name
        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void deleteProduct() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Test products are ordered by name
        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        ProductWithCategory productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNotNull(productWithCategory);
        assertThat(productWithCategory.getProduct(), is(TestData.PRODUCT_2));

        productViewModel.deleteProduct(productWithCategory.getProduct());
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategory = LiveDataTestUtil.getValue(productDao.getProductWithCategoryByProductId(TestData.PRODUCT_2.getId()));

        assertNull(productWithCategory);

        // Test products are still ordered by name
        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void deleteMultipleProductsFromProductsWithCategoriesArray() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size()));

        // Test products are ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        ProductWithCategory[] productWithCategoriesToDeleteArray = {TestData.PRODUCT_1_WITH_CATEGORY, TestData.PRODUCT_2_WITH_CATEGORY};

        assertTrue(productWithCategories.contains(productWithCategoriesToDeleteArray[0]));
        assertTrue(productWithCategories.contains(productWithCategoriesToDeleteArray[1]));

        productViewModel.deleteMultipleProductsFromProductsWithCategories(productWithCategoriesToDeleteArray);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size() - productWithCategoriesToDeleteArray.length));

        // Test products are still ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        assertFalse(productWithCategories.contains(productWithCategoriesToDeleteArray[0]));
        assertFalse(productWithCategories.contains(productWithCategoriesToDeleteArray[1]));
    }

    @Test
    public void deleteMultipleProductsFromProductsWithCategoriesList() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size()));

        // Test products are ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        List<ProductWithCategory> productWithCategoriesToDeleteList = new ArrayList<>();
        productWithCategoriesToDeleteList.add(TestData.PRODUCT_1_WITH_CATEGORY);
        productWithCategoriesToDeleteList.add(TestData.PRODUCT_2_WITH_CATEGORY);

        assertTrue(productWithCategories.containsAll(productWithCategoriesToDeleteList));

        productViewModel.deleteMultipleProductsFromProductsWithCategories(productWithCategoriesToDeleteList);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size() - productWithCategoriesToDeleteList.size()));

        // Test products are still ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        assertFalse(productWithCategories.contains(productWithCategoriesToDeleteList.get(0)));
        assertFalse(productWithCategories.contains(productWithCategoriesToDeleteList.get(1)));
    }

    @Test
    public void deleteMultipleProductsArray() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size()));

        // Test products are ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        Product[] productsToDeleteArray = {TestData.PRODUCT_1, TestData.PRODUCT_2};

        List<Product> products = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productWithCategories) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.contains(productsToDeleteArray[0]));
        assertTrue(products.contains(productsToDeleteArray[1]));

        productViewModel.deleteMultipleProducts(productsToDeleteArray);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size() - productsToDeleteArray.length));

        // Test products are still ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        products = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productWithCategories) {
            products.add(productWithCategory.getProduct());
        }

        assertFalse(products.contains(productsToDeleteArray[0]));
        assertFalse(products.contains(productsToDeleteArray[1]));
    }

    @Test
    public void deleteMultipleProductsList() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size()));

        // Test products are ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        List<Product> productsToDeleteList = new ArrayList<>();
        productsToDeleteList.add(TestData.PRODUCT_1);
        productsToDeleteList.add(TestData.PRODUCT_2);

        List<Product> products = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productWithCategories) {
            products.add(productWithCategory.getProduct());
        }

        assertTrue(products.containsAll(productsToDeleteList));

        productViewModel.deleteMultipleProducts(productsToDeleteList);
        TimeUnit.MILLISECONDS.sleep(100);

        productWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productWithCategories.size(), is(TestData.PRODUCTS.size() - productsToDeleteList.size()));

        // Test products are still ordered by name
        for (int i = 1; i < productWithCategories.size(); i++) {
            String currentName = productWithCategories.get(i).getProduct().getName();
            String previousName = productWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        products = new ArrayList<>();

        for (ProductWithCategory productWithCategory : productWithCategories) {
            products.add(productWithCategory.getProduct());
        }

        assertFalse(products.contains(productsToDeleteList.get(0)));
        assertFalse(products.contains(productsToDeleteList.get(1)));
    }

    @Test
    public void reorderProductListByNameAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
        }

        newOrdering = OrderByEnum.NAME_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void reorderProductListByNameDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
        }
    }

    @Test
    public void reorderProductListByBarcodeAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) >= 0);
        }
    }

    @Test
    public void reorderProductListByBarcodeDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) <= 0);
        }
    }

    @Test
    public void reorderProductListByPriceAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
        }
    }

    @Test
    public void reorderProductListByPriceDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) <= 0);
        }
    }

    @Test
    public void updateDatabaseProductListAndTestIfSortedListIsUpdatedAndReorderedCorrectly() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        productViewModel.addProduct(TestData.PRODUCT_TO_ADD_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size() + 1));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void reorderProductListByBarcodeAscThenUpdateDatabaseProductListAndTestIfSortedListIsUpdatedAndSortedByBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) >= 0);
        }

        productViewModel.addProduct(TestData.PRODUCT_TO_ADD_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) >= 0);
        }
    }

    @Test
    public void reorderProductListByPriceAscThenRepopulateListWithAllProductsAndTestReorderingIsBackToNaturalOrdering() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
        }

        productViewModel.populateListWithAllProductsWithCategories();

        OrderByEnum currentOrdering = productViewModel.getCurrentOrdering();
        OrderByEnum naturalOrdering = productViewModel.getNaturalOrdering();

        assertSame(naturalOrdering, currentOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void searchProductsByExactBarcode() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByExactBarcodeThenRepopulateWithAllProductsWithCategories() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        productViewModel.populateListWithAllProductsWithCategories();

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(TestData.PRODUCTS.size()));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByNameAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
        }

        newOrdering = OrderByEnum.NAME_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByNameDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByBarcodeAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByBarcodeDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) <= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByPriceAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByPriceDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) <= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void searchProductsByBarcodeAndReorderByPriceAscThenAddNewProductWhichFitsSearchCriteriaAndTestOrdering() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 2's barcode is shared with product 5, so should result in a list of size 2.
        String barcode = TestData.PRODUCT_2.getBarcode();
        int listSize = 2;

        productViewModel.searchProductsWithCategoryByBarcode(barcode);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }

        productViewModel.addProduct(TestData.PRODUCT_TO_ADD_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize + 1));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
            assertEquals(barcode, productsWithCategories.get(i).getProduct().getBarcode());
        }
    }

    @Test
    public void filterProductsByCategoryId() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByNameAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        newOrdering = OrderByEnum.NAME_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByNameDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByBarcodeAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByBarcodeDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentBarcode = productsWithCategories.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategories.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) <= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByPriceAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByPriceDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) <= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }
    }

    @Test
    public void filterProductsByCategoryIdAndReorderByPriceAscThenAddNewProductThatMeetsSearchCriterionAndTestOrdering() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        // Product 1's category id is shared with product 2, so should result in a list of size 2
        long categoryId = TestData.PRODUCT_1.getCategoryId();
        int listSize = 2;

        productViewModel.filterProductsWithCategoryByCategoryId(categoryId);

        List<ProductWithCategory> productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            String currentName = productsWithCategories.get(i).getProduct().getName();
            String previousName = productsWithCategories.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
        }

        productViewModel.addProduct(TestData.PRODUCT_TO_ADD_1);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategories = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategories.size(), is(listSize + 1));

        for (int i = 1; i < productsWithCategories.size(); i++) {
            Price currentPrice = productsWithCategories.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategories.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
            assertSame(categoryId, productsWithCategories.get(i).getProduct().getCategoryId());
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
        productViewModel.searchProductsWithCategory(barcode, startPartialName, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        // Search with the partial name from middle of the product name
        productViewModel.searchProductsWithCategory(barcode, midPartialName, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        // Search with the partial name from start of the product name
        productViewModel.searchProductsWithCategory(barcode, endPartialName, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, exactName, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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
        productViewModel.searchProductsWithCategory(startPartialBarcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));

        // Search with the partial barcode from middle of the product barcode
        productViewModel.searchProductsWithCategory(midPartialBarcode, name, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_4));

        // Search with the partial barcode from end of the product barcode
        productViewModel.searchProductsWithCategory(endPartialBarcode, name, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        // Only product 3 should have the category id associated with product 3, so searching should return 1 result.
        int listSize = 1;

        assertThat(productsWithCategory.size(), is(listSize));
        assertThat(productsWithCategory.get(0).getProduct(), is(TestData.PRODUCT_3));

        categoryId = TestData.PRODUCT_1.getCategoryId();

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

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
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByNameAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
        }

        newOrdering = OrderByEnum.NAME_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByNameDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.NAME_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) <= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByBarcodeAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentBarcode = productsWithCategory.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategory.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) >= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByBarcodeDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.BARCODE_DESC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentBarcode = productsWithCategory.get(i).getProduct().getBarcode();
            String previousBarcode = productsWithCategory.get(i - 1).getProduct().getBarcode();

            assertTrue(currentBarcode.compareTo(previousBarcode) <= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByPriceAsc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            Price currentPrice = productsWithCategory.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategory.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByPriceDesc() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            Price currentPrice = productsWithCategory.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategory.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchThenAddNewProductThatFitsSearchAndTestForUpdatedList() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        productViewModel.addProduct(TestData.PRODUCT_TO_ADD_2);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategory.size(), is(listSize + 1));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }
    }

    @Test
    public void searchProductsWithCategoriesByRealisticSearchAndReorderByPriceAscThenAddNewProductThatFitsSearchAndTestForUpdatedListWithCorrectOrdering() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        productDao.insertMultiple(TestData.PRODUCTS.toArray(new Product[TestData.PRODUCTS.size()]));

        Product product = LiveDataTestUtil.getValue(productDao.getProductById(TestData.PRODUCT_3.getId()));
        assertNotNull(product);

        String barcode = "12345";
        String name = "p";
        long categoryId = 0;
        float lowerPrice = 5.49f;
        float higherPrice = 20.27f;

        productViewModel.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
        List<ProductWithCategory> productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory: productsWithCategory) {
            products.add(productWithCategory.getProduct());
        }

        int listSize = 2;
        assertThat(productsWithCategory.size(), is(listSize));

        assertTrue(products.contains(TestData.PRODUCT_1));
        assertTrue(products.contains(TestData.PRODUCT_3));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            String currentName = productsWithCategory.get(i).getProduct().getName();
            String previousName = productsWithCategory.get(i - 1).getProduct().getName();

            assertTrue(currentName.compareTo(previousName) >= 0);
        }

        OrderByEnum newOrdering = OrderByEnum.PRICE_ASC;
        productViewModel.reorderProductList(newOrdering);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        for (int i = 1; i < productsWithCategory.size(); i++) {
            Price currentPrice = productsWithCategory.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategory.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
        }

        productViewModel.addProduct(TestData.PRODUCT_TO_ADD_2);
        TimeUnit.MILLISECONDS.sleep(100);

        productsWithCategory = LiveDataTestUtil.getValue(productViewModel.getProductsWithCategories());

        assertThat(productsWithCategory.size(), is(listSize + 1));

        for (int i = 1; i < productsWithCategory.size(); i++) {
            Price currentPrice = productsWithCategory.get(i).getProduct().getPrice();
            Price previousPrice = productsWithCategory.get(i - 1).getProduct().getPrice();

            assertTrue(currentPrice.compareTo(previousPrice) >= 0);
        }
    }
}
