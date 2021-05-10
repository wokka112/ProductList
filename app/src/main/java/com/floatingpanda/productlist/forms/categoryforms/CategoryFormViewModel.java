package com.floatingpanda.productlist.forms.categoryforms;

import android.app.Application;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.AndroidViewModel;

import com.floatingpanda.productlist.callbackinterfaces.CategoryFormCallback;
import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.repositories.CategoryRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
//TODO write tests

public class CategoryFormViewModel extends AndroidViewModel {
    private CategoryRepository categoryRepository;
    private CategoryForm categoryForm;
    private View.OnFocusChangeListener onFocusName;

    // Tracks current category's id. Should be set when editing categories. When adding new categories should be left as 0.
    private int categoryId = 0;

    public CategoryFormViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
    }

    // For testing purposes
    public CategoryFormViewModel(@NonNull Application application, AppDatabase db) {
        super(application);
        categoryRepository = new CategoryRepository(db);
    }

    private void setCategoryId(int id) {
        categoryId = id;
    }

    public void init() {
        categoryForm = new CategoryForm();

        onFocusName = (View.OnFocusChangeListener) (v, hasFocus) -> {
            // Once focus leaves the category name edittext, test for form validity
            if (!hasFocus) {
                String categoryName = categoryForm.getFields().getName();
                Log.w("CategoryFormViewModel", "Focus changed. Category name: " + categoryName);

                // If the name edittext is empty
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    Log.w("CategoryFormViewModel", "Category name empty. Category name: " + categoryName);
                    // Set name exists in the database to false, because an empty name cannot be
                    // entered into the database and thus does not exist in it.
                    categoryForm.setNameExistsInDatabase(false);

                    // Immediately test for form validity, which will result in an error message because
                    // name cannot be empty.
                    categoryForm.isNameValid(true);
                } // Otherwise if name is not empty
                else {
                    Log.w("CategoryFormViewModel", "Category name exists. Category name: " + categoryName);
                    // Test if a category with that name exists in the database
                    categoryRepository.containsCategory(categoryName, new CategoryFormCallback() {
                        public void runNameValidityTest(boolean nameExists) {
                            Log.w("CategoryFormViewModel", "Category name exists: " + nameExists + " Category name: " + categoryName);
                            // Set whether or not the name exists in the database
                            categoryForm.setNameExistsInDatabase(nameExists);
                            // Test for form validity
                            categoryForm.isNameValid(true);
                        }
                    });
                }
            }
        };
    }

    public Category getCategory() {
        Category category;
        if (categoryId > 0) {
            category = new Category(categoryId, categoryForm.getFields().getName());
        } else {
            category = new Category(categoryForm.getFields().getName());
        }

        return category;
    }

    public CategoryForm getCategoryForm() { return categoryForm; }

    public View.OnFocusChangeListener getOnFocusName() { return onFocusName; }

    @BindingAdapter("error")
    public static void setError(TextInputLayout textInputLayout, Integer resId) {
        if (resId != null) {
            textInputLayout.setError(textInputLayout.getContext().getString(resId));
        }
    }

    @BindingAdapter("onFocus")
    public static void bindFocusChange(TextInputEditText editText, View.OnFocusChangeListener onFocusChangeListener) {
        if (editText.getOnFocusChangeListener() == null) {
            editText.setOnFocusChangeListener(onFocusChangeListener);
        }
    }
}
