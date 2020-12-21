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

import static org.junit.Assert.assertEquals;
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
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class CategoryDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;

    private CategoryDao categoryDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void getAllCategoriesWhenNoneInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertTrue(categories.isEmpty());
    }

    @Test
    public void getAllCategoriesWhenInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
    }

    @Test
    public void insertCategoryWhenNoneInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertTrue(categories.isEmpty());

        categoryDao.insert(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));
    }

    @Test
    public void insertCategoryWhenAnotherCategoryAlreadyInserted() throws InterruptedException {
        categoryDao.insert(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));

        categoryDao.insert(TestData.CATEGORY_2);
        TimeUnit.MILLISECONDS.sleep(100);

        List<Category> newCategories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(newCategories.size(), is(categories.size() + 1));
    }

    @Test
    public void insertCategoryWhenItWasAlreadyInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertTrue(categories.isEmpty());

        categoryDao.insert(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));

        categoryDao.insert(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));
    }

    @Test
    public void insertMultipleCategoriesWhenNoneInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertTrue(categories.isEmpty());

        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        for (Category category : categories) {
            assertTrue(TestData.CATEGORIES.contains(category));
        }
    }

    @Test
    public void insertMultipleCategoriesWhenAnotherCategoryAlreadyInserted() throws InterruptedException {
        categoryDao.insert(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));

        List<Category> categoriesToAdd = new ArrayList<>();
        categoriesToAdd.add(TestData.CATEGORY_2);
        categoriesToAdd.add(TestData.CATEGORY_3);

        categoryDao.insertMultiple(categoriesToAdd.toArray(new Category[categoriesToAdd.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        List<Category> newCategories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(newCategories.size(), is(categories.size() + categoriesToAdd.size()));
    }

    @Test
    public void insertMultipleCategoriesWhenTheyWereAlreadyInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertTrue(categories.isEmpty());

        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        for (Category category : categories) {
            assertTrue(TestData.CATEGORIES.contains(category));
        }

        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        for (Category category : categories) {
            assertTrue(TestData.CATEGORIES.contains(category));
        }
    }

    @Test
    public void updateCategory() throws InterruptedException {
        categoryDao.insert(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));

        String newName = "New Name";
        Category newCategory = new Category(TestData.CATEGORY_1.getId(), newName);

        categoryDao.update(newCategory);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(not(TestData.CATEGORY_1)));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));
        assertThat(categories.get(0).getName(), is(newName));
    }

    @Test
    public void deleteCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
        assertTrue(categories.contains(TestData.CATEGORY_1));

        categoryDao.delete(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - 1));
        assertFalse(categories.contains(TestData.CATEGORY_1));
    }

    @Test
    public void deleteMultipleCategories() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
        assertTrue(categories.contains(TestData.CATEGORY_1));
        assertTrue(categories.contains(TestData.CATEGORY_2));

        List<Category> categoriesToDelete = Arrays.asList(TestData.CATEGORY_1, TestData.CATEGORY_2);

        categoryDao.deleteMultiple(categoriesToDelete.toArray(new Category[categoriesToDelete.size()]));
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - categoriesToDelete.size()));
        assertFalse(categories.contains(TestData.CATEGORY_1));
        assertFalse(categories.contains(TestData.CATEGORY_2));
    }

    @Test
    public void deleteAllCategories() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        categoryDao.deleteAll();
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryDao.getAll());

        assertTrue(categories.isEmpty());
    }
}
