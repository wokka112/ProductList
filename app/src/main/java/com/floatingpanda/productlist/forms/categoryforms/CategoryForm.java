package com.floatingpanda.productlist.forms.categoryforms;

import android.util.Log;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.floatingpanda.productlist.BR;
import com.floatingpanda.productlist.R;

public class CategoryForm extends BaseObservable {
    private CategoryFormFields fields;
    private CategoryFormErrorFields errors;
    private MutableLiveData<CategoryFormFields> buttonClick = new MutableLiveData<>();
    // Tracks whether the current category name in the form exists in the database already
    private boolean nameExistsInDatabase = false;

    public CategoryForm() {
        fields = new CategoryFormFields();
        errors = new CategoryFormErrorFields();
    }

    public void setNameExistsInDatabase(boolean nameExistsInDatabase) {
        Log.w("CategoryForm", "SetNameExistsInDatabase called with " + nameExistsInDatabase);
        this.nameExistsInDatabase = nameExistsInDatabase;
    }

    @Bindable
    /**
     * Tests whether the form is valid and returns true if it is, false if it is not.
     */
    public boolean isValid() {
        boolean valid = isNameValid(false);

        notifyPropertyChanged(BR.nameError);

        return valid;
    }

    /**
     * Tests for validity of the name entered in the category form.
     *
     * If the name is empty, then the form is invalid.
     * If a category with that name already exists in the database, then the form is invalid.
     * @param setMessage determines whether to set an error message for the name edittext
     * @return
     */
    public boolean isNameValid(boolean setMessage) {
        String name = fields.getName();
        Log.w("CategoryForm", "isNameValid called");

        // If a name is entered into the name field that does not exist in the database
        if (name != null && !nameExistsInDatabase && !name.trim().isEmpty()) {
            Log.w("CategoryForm", "Name is valid");
            // Set errors to null
            errors.setName(null);
            notifyPropertyChanged(BR.valid);

            // Return true, i.e. the name is valid
            return true;
        } // Otherwise the name entered is invalid

        Log.w("CategoryForm", "Name is invalid");
        if (setMessage) {
            if (name == null || name.trim().isEmpty()) {
                Log.w("CategoryForm", "Name is empty");
                errors.setName(R.string.category_name_general_error);
            } else if (nameExistsInDatabase) {
                Log.w("CategoryForm", "Name exists in database");
                errors.setName(R.string.category_name_exists_error);
            }
        }

        notifyPropertyChanged(BR.valid);

        // Return false to show name is invalid
        return false;
    }

    public MutableLiveData<CategoryFormFields> getCategoryFormFields() { return buttonClick; }

    public CategoryFormFields getFields() { return fields; }

    @Bindable
    public Integer getNameError() { return errors.getName(); }
}
