package com.floatingpanda.productlist.ui.products;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.floatingpanda.productlist.R;
import com.floatingpanda.productlist.ui.base.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProductListFragment extends BaseFragment {
    private ProductViewModel productViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_list, container, false);

        productViewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);
        super.setViewModel(productViewModel);

        FloatingActionButton addFab, searchFab;

        addFab = root.findViewById(R.id.product_add_fab);
        searchFab = root.findViewById(R.id.product_search_fab);

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAddProduct();
            }
        });

        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBarcodeSearch();
            }
        });

        //TODO remove once implemented list
        Button detailsButton;
        detailsButton = root.findViewById(R.id.product_details_button);

        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProductDetails();
            }
        });

        return root;
    }

    private void navigateToAddProduct() {
        productViewModel.navigate(ProductListFragmentDirections.actionNavProductListToNavProductAdd());
    }

    private void navigateToProductDetails() {
        productViewModel.navigate(ProductListFragmentDirections.actionNavProductListToNavProductDetails());
    }

    private void navigateToBarcodeSearch() {
        productViewModel.navigate(ProductListFragmentDirections.actionNavProductListToNavProductSearchBarcode());
    }
}
