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
import com.floatingpanda.productlist.databinding.FragmentCategoryListBinding;
import com.floatingpanda.productlist.ui.base.BaseFragment;
import com.floatingpanda.productlist.ui.products.ProductViewModel;

public class CategoryListFragment extends BaseFragment {
    private CategoryViewModel categoryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCategoryListBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_category_list, container, false);
        View root = binding.getRoot();

        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);
        super.setViewModel(categoryViewModel);

        binding.setViewModel(categoryViewModel);

        return root;
    }
}
