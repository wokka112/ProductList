package com.floatingpanda.productlist.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.floatingpanda.productlist.navcommands.Back;
import com.floatingpanda.productlist.navcommands.BackTo;
import com.floatingpanda.productlist.navcommands.NavigationCommand;
import com.floatingpanda.productlist.navcommands.To;
import com.floatingpanda.productlist.navcommands.ToRoot;
import com.floatingpanda.productlist.R;

public class BaseFragment extends Fragment {
    BaseViewModel viewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (viewModel != null) {
            Log.w("BaseFragment", "Viewmodel was not null");
            viewModel.getNavigationCommands().observe(getViewLifecycleOwner(), new Observer<NavigationCommand>() {
                @Override
                public void onChanged(NavigationCommand navigationCommand) {
                    NavController navController = Navigation.findNavController(view);

                    if (navigationCommand.getClass() == To.class) {
                        navController.navigate(((To) navigationCommand).getNavDirections());
                    } else if (navigationCommand.getClass() == Back.class) {
                        navController.popBackStack();
                    } else if (navigationCommand.getClass() == BackTo.class) {
                        navController.popBackStack(((BackTo) navigationCommand).getDestinationId(), true);
                    } else if (navigationCommand.getClass() == ToRoot.class) {
                        navController.navigate(R.id.nav_product_list);
                    }
                }
            });
        } else {
            Log.w("BaseFragment", "Viewmodel was null");
        }
    }

    protected void setViewModel(BaseViewModel viewModel) {
        this.viewModel = viewModel;
    }
}
