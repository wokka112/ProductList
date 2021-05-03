package com.floatingpanda.productlist.ui.categories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.repositories.CategoryRepository;
import com.floatingpanda.productlist.ui.base.BaseViewModel;

import java.util.List;

public class CategoryViewModel extends BaseViewModel {
    private CategoryRepository categoryRepository;
    private LiveData<List<Category>> categories;

    public CategoryViewModel(Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        categories = categoryRepository.getAllCategories();
    }

    // Used for testing purposes.
    public CategoryViewModel(Application application, AppDatabase database) {
        super(application);
        categoryRepository = new CategoryRepository(database);
        categories = categoryRepository.getAllCategories();
    }

    public LiveData<List<Category>> getCategories() { return categories; }

    public void addCategory(Category category) {
        categoryRepository.addCategory(category);
    }

    public void addCategoryAndGoBack() {
        Log.w("CategoryViewModel.java", "Called add category and go back with null category.");
        super.back();
    }

    public void editCategory(Category category) {
        categoryRepository.editCategory(category);
    }

    public void deleteCategory(Category category) {
        categoryRepository.deleteCategory(category);
    }

    public void deleteCategories(List<Category> categoriesToDelete) {
        deleteCategories(categoriesToDelete.toArray(new Category[categoriesToDelete.size()]));
    }

    public void deleteCategories(Category... categoriesToDelete) {
        categoryRepository.deleteCategories(categoriesToDelete);
    }

    public void deleteAllCategories() {
        categoryRepository.deleteAllCategories();
    }
}
