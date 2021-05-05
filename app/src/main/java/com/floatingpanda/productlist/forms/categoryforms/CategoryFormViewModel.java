package com.floatingpanda.productlist.forms.categoryforms;

import android.view.View;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.ViewModel;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CategoryFormViewModel extends ViewModel {
    private CategoryForm categoryForm;
    private View.OnFocusChangeListener onFocusName;

    public void init() {
        categoryForm = new CategoryForm();

        onFocusName = (View.OnFocusChangeListener) (v, hasFocus) -> {
            TextInputEditText editText = (TextInputEditText) v;
            if (editText.getText().toString().length() > 0 && !hasFocus) {
                categoryForm.setNameValidity(false);
            }
        };
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
