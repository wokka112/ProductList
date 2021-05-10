package com.floatingpanda.productlist.ui.products;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.floatingpanda.productlist.R;
import com.floatingpanda.productlist.databinding.FragmentProductListBinding;
import com.floatingpanda.productlist.ui.base.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductListFragment extends BaseFragment {
    private ProductViewModel productViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FragmentProductListBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_product_list, container, false);
        View root = binding.getRoot();

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        super.setViewModel(productViewModel);

        binding.setViewModel(productViewModel);

        return root;
    }
}
