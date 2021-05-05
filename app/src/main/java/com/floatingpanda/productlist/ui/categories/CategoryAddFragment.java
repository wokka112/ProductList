package com.floatingpanda.productlist.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.floatingpanda.productlist.R;
import com.floatingpanda.productlist.databinding.FragmentCategoryAddEditBinding;
import com.floatingpanda.productlist.databinding.FragmentCategoryListBinding;
import com.floatingpanda.productlist.databinding.FragmentProductListBinding;
import com.floatingpanda.productlist.forms.categoryforms.CategoryFormViewModel;
import com.floatingpanda.productlist.ui.base.BaseFragment;

public class CategoryAddFragment extends BaseFragment {
    private CategoryViewModel categoryViewModel;
    private CategoryFormViewModel categoryFormViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCategoryAddEditBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_category_add_edit, container, false);
        View root = binding.getRoot();

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        super.setViewModel(categoryViewModel);

        categoryFormViewModel = new ViewModelProvider((this)).get(CategoryFormViewModel.class);
        categoryFormViewModel.init();

        binding.setCategoryViewModel(categoryViewModel);
        binding.setCategoryFormViewModel(categoryFormViewModel);

        return root;
    }
}
