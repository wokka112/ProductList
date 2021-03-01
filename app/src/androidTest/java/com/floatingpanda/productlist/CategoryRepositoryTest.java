package com.floatingpanda.productlist;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.CategoryDao;
import com.floatingpanda.productlist.repositories.CategoryRepository;

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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

//TODO look into removing the hack-y TimeUnit.MILLISECONDS.sleep() functions.
// Need to find a way to wait until the database is updated before testing again. Maybe use observers?
// Could use LiveDataTestUtil.waitforUpdate() method.

@RunWith(AndroidJUnit4.class)
public class CategoryRepositoryTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;

    private CategoryDao categoryDao;
    private CategoryRepository categoryRepository;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
        categoryRepository = new CategoryRepository(db);
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void getLiveCategoriesWhenNoneInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertTrue(categories.isEmpty());
    }

    @Test
    public void getLiveCategoriesWhenAllInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
    }

    @Test
    public void addCategoryWhenNoneInserted() throws InterruptedException {
        categoryRepository.addCategory(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(1));
    }

    @Test
    public void addCategoryWhenSomeInserted() throws InterruptedException {
        categoryDao.insert(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(1));

        categoryRepository.addCategory(TestData.CATEGORY_2);
        TimeUnit.MILLISECONDS.sleep(100);

        int initialSize = categories.size();
        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(initialSize + 1));
    }

    @Test
    public void addCategoryWhenItAlreadyExistsInDatabase() throws InterruptedException {
        categoryDao.insert(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));

        categoryRepository.addCategory(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
    }

    @Test
    public void addMultipleCategoriesWhenNoneExistInDatabase() throws InterruptedException {
        List<Category> categoriesToAdd = new ArrayList<>();
        categoriesToAdd.add(TestData.CATEGORY_1);
        categoriesToAdd.add(TestData.CATEGORY_2);
        categoryRepository.addCategories(categoriesToAdd.toArray(new Category[categoriesToAdd.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(categoriesToAdd.size()));
    }

    @Test
    public void addMultipleCategoriesWhenSomeOtherCategoriesExistInDatabaseAlready() throws InterruptedException {
        categoryDao.insert((TestData.CATEGORY_3));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(1));

        List<Category> categoriesToAdd = new ArrayList<>();
        categoriesToAdd.add(TestData.CATEGORY_1);
        categoriesToAdd.add(TestData.CATEGORY_2);
        categoryRepository.addCategories(categoriesToAdd.toArray(new Category[categoriesToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        int originalCategoriesSize = categories.size();
        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(originalCategoriesSize + categoriesToAdd.size()));
    }

    @Test
    public void addMultipleCategoriesWhenTheyAlreadyExistInDatabase() throws InterruptedException {
        List<Category> categoriesToAdd = new ArrayList<>();
        categoriesToAdd.add(TestData.CATEGORY_1);
        categoriesToAdd.add(TestData.CATEGORY_2);
        categoryRepository.addCategories(categoriesToAdd.toArray(new Category[categoriesToAdd.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(categoriesToAdd.size()));

        // Prove that the database only contains the categories in categoriesToAdd
        for (Category category : categories) {
            assertTrue(categoriesToAdd.contains(category));
        }

        // Try to add the same categories that have just been added
        categoryRepository.addCategories(categoriesToAdd.toArray(new Category[categoriesToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        // Assert that no new categories have been added and that the size of the database is the same as it was
        // before the second addition.
        assertThat(categories.size(), is(categoriesToAdd.size()));

        // Prove that the database still only contains the categories in categoriesToAdd
        for (Category category : categories) {
            assertTrue(categoriesToAdd.contains(category));
        }
    }

    @Test
    public void editCategoryInDatabase() throws InterruptedException {
        categoryDao.insert(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));

        Category originalCategory = categories.get(0);
        Category editedCategory = new Category(originalCategory.getId(), originalCategory.getName());
        editedCategory.setName("Changed name");
        categoryRepository.editCategory(editedCategory);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(1));
        // Updated category should still have same id as original.
        assertThat(categories.get(0).getId(), is(editedCategory.getId()));
        assertThat(categories.get(0).getId(), is(originalCategory.getId()));

        // Updated category in the database will have a different name compared to the original category
        assertThat(categories.get(0).getName(), is(editedCategory.getName()));
        assertThat(categories.get(0).getName(), is(not(originalCategory.getName())));
    }

    @Test
    public void deleteCategoryFromDatabase() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        categoryRepository.deleteCategory(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - 1));
        assertFalse(categories.contains(TestData.CATEGORY_1));
    }

    @Test
    public void deleteMultipleCategoriesFromDatabase() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        List<Category> categoriesToDelete = new ArrayList<>();
        categoriesToDelete.add(TestData.CATEGORY_1);
        categoriesToDelete.add(TestData.CATEGORY_2);

        categoryRepository.deleteCategories(categoriesToDelete.toArray(new Category[categoriesToDelete.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - categoriesToDelete.size()));

        // Test that each category has been deleted from the database
        for (Category category : categoriesToDelete) {
            assertFalse(categories.contains(category));
        }
    }

    @Test
    public void deleteAllCategoriesFromDatabaseWhenNoneInserted() throws InterruptedException {
        categoryRepository.deleteAllCategories();
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertTrue(categories.isEmpty());
    }

    @Test
    public void deleteAllCategoriesFromDatabaseWhenAllInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        categoryRepository.deleteAllCategories();
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryRepository.getAllCategories());

        assertTrue(categories.isEmpty());
    }
}
