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
import com.floatingpanda.productlist.ui.categories.CategoryViewModel;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CategoryViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;

    private CategoryDao categoryDao;
    private CategoryViewModel categoryViewModel;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
        categoryViewModel= new CategoryViewModel(ApplicationProvider.getApplicationContext(), db);
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void getCategoriesWhenNoneInserted() throws InterruptedException {
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertTrue(categories.isEmpty());
    }

    @Test
    public void getCategoriesWhenAllInserted() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
    }

    @Test
    public void addCategoryWhenNoneInserted() throws InterruptedException {
        categoryViewModel.addCategory(TestData.CATEGORY_2);
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_2));
    }

    @Test
    public void addCategoryWhenSomeInserted() throws InterruptedException {
        categoryViewModel.addCategory(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));

        categoryViewModel.addCategory(TestData.CATEGORY_2);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(2));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
        assertThat(categories.get(1), is(TestData.CATEGORY_2));
    }

    @Test
    public void addSameCategoryTwice() throws InterruptedException {
        categoryViewModel.addCategory(TestData.CATEGORY_1);
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));

        categoryViewModel.addCategory(TestData.CATEGORY_1);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0), is(TestData.CATEGORY_1));
    }

    @Test
    public void editCategory() throws InterruptedException {
        categoryViewModel.addCategory(TestData.CATEGORY_1);

        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));
        assertThat(categories.get(0).getName(), is(TestData.CATEGORY_1.getName()));

        Category editedCategory = new Category(TestData.CATEGORY_1.getId(), TestData.CATEGORY_1.getName());
        editedCategory.setName("New name woooooooo");

        categoryViewModel.editCategory(editedCategory);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(1));
        assertThat(categories.get(0).getId(), is(TestData.CATEGORY_1.getId()));
        assertThat(categories.get(0).getName(), is(not(TestData.CATEGORY_1.getName())));
        assertThat(categories.get(0).getName(), is(editedCategory.getName()));
    }

    @Test
    public void deleteCategory() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));

        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
        assertTrue(categories.contains(TestData.CATEGORY_2));

        categoryViewModel.deleteCategory(TestData.CATEGORY_2);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - 1));
        assertFalse(categories.contains(TestData.CATEGORY_2));
    }

    @Test
    public void deleteCategoryWhenItDoesNotExist() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));
        assertTrue(categories.contains(TestData.CATEGORY_2));

        categoryViewModel.deleteCategory(TestData.CATEGORY_2);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - 1));
        assertFalse(categories.contains(TestData.CATEGORY_2));

        categoryViewModel.deleteCategory(TestData.CATEGORY_2);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - 1));
        assertFalse(categories.contains(TestData.CATEGORY_2));
    }

    @Test
    public void deleteMultipleCategoriesViaArray() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        Category[] categoriesToDelete = { TestData.CATEGORY_1, TestData.CATEGORY_2 };

        categoryViewModel.deleteCategories(categoriesToDelete);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - categoriesToDelete.length));
        assertFalse(categories.containsAll(Arrays.asList(categoriesToDelete)));
    }

    @Test
    public void deleteMultipleCategoriesViaList() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        List<Category> categoriesToDelete = new ArrayList<>();
        categoriesToDelete.add(TestData.CATEGORY_1);
        categoriesToDelete.add(TestData.CATEGORY_2);

        categoryViewModel.deleteCategories(categoriesToDelete);
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size() - categoriesToDelete.size()));
        assertFalse(categories.containsAll(categoriesToDelete));
    }

    @Test
    public void deleteAllCategories() throws InterruptedException {
        categoryDao.insertMultiple(TestData.CATEGORIES.toArray(new Category[TestData.CATEGORIES.size()]));
        List<Category> categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertThat(categories.size(), is(TestData.CATEGORIES.size()));

        categoryDao.deleteAll();
        TimeUnit.MILLISECONDS.sleep(100);

        categories = LiveDataTestUtil.getValue(categoryViewModel.getCategories());

        assertTrue(categories.isEmpty());
    }
}
