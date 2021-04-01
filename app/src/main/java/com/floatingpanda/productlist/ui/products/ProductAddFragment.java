package com.floatingpanda.productlist.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.floatingpanda.productlist.R;
import com.floatingpanda.productlist.ui.base.BaseFragment;

public class ProductAddFragment extends BaseFragment {
    private ProductViewModel productViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_add_edit, container, false);

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        super.setViewModel(productViewModel);

        return root;
    }

    private void navigateBackToProductList() {
        // Add new product via viewmodel
        // Return to product list
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }
}
