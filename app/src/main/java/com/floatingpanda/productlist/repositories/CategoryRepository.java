package com.floatingpanda.productlist.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.CategoryDao;

import java.util.List;

public class CategoryRepository {
    private CategoryDao categoryDao;

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        categoryDao = db.categoryDao();
    }

    // Used for tests
    public CategoryRepository(AppDatabase appDatabase) {
        categoryDao = appDatabase.categoryDao();
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAll();
    }

    public void addCategory(Category category) {
        AppDatabase.getExecutorService().execute(() -> {
            categoryDao.insert(category);
        });
    }

    public void addCategories(Category... categories) {
        AppDatabase.getExecutorService().execute(() -> {
            categoryDao.insertMultiple(categories);
        });
    }

    public void editCategory(Category category) {
        AppDatabase.getExecutorService().execute(() -> {
            categoryDao.update(category);
        });
    }

    public void deleteCategory(Category category) {
        AppDatabase.getExecutorService().execute(() -> {
            categoryDao.delete(category);
        });
    }

    public void deleteCategories(Category... categories) {
        AppDatabase.getExecutorService().execute(() -> {
            categoryDao.deleteMultiple(categories);
        });
    }

    public void deleteAllCategories() {
        AppDatabase.getExecutorService().execute(() -> {
            categoryDao.deleteAll();
        });
    }
}
