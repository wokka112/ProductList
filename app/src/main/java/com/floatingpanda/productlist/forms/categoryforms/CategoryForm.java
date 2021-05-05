package com.floatingpanda.productlist.forms.categoryforms;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.floatingpanda.productlist.BR;
import com.floatingpanda.productlist.R;

public class CategoryForm extends BaseObservable {
    private CategoryFormFields fields;
    private CategoryFormErrorFields errors;
    private MutableLiveData<CategoryFormFields> buttonClick = new MutableLiveData<>();

    public CategoryForm() {
        fields = new CategoryFormFields();
        errors = new CategoryFormErrorFields();
    }

    @Bindable
    public boolean isValid() {
        boolean valid = isNameValid();

        notifyPropertyChanged(BR.nameError);

        return valid;
    }

    public boolean isNameValid() {
        if (getNameError() == null) {
            return true;
        }

        return false;
    }

    public void setNameValidity(boolean nameExists) {
        String name = fields.getName();

        if (name != null && !nameExists && !name.trim().isEmpty()) {
            errors.setName(null);
            notifyPropertyChanged(BR.nameError);
            notifyPropertyChanged(BR.valid);
        } else {
            if (nameExists) {
                errors.setName(R.string.category_name_exists_error);
            } else if (name == null || name.trim().isEmpty()) {
                errors.setName(R.string.category_name_general_error);
            }

            notifyPropertyChanged(BR.nameError);
            notifyPropertyChanged(BR.valid);
        }
    }

    public MutableLiveData<CategoryFormFields> getCategoryFormFields() { return buttonClick; }

    public CategoryFormFields getFields() { return fields; }

    @Bindable
    public Integer getNameError() { return errors.getName(); }
}
