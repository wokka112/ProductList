package com.floatingpanda.productlist.ui.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavDirections;

import com.floatingpanda.productlist.navcommands.Back;
import com.floatingpanda.productlist.navcommands.BackTo;
import com.floatingpanda.productlist.navcommands.NavigationCommand;
import com.floatingpanda.productlist.navcommands.To;
import com.floatingpanda.productlist.navcommands.ToRoot;
import com.floatingpanda.productlist.other.SingleLiveEvent;

public class BaseViewModel extends AndroidViewModel {
    private SingleLiveEvent<NavigationCommand> navigationCommands;

    public BaseViewModel(@NonNull Application application) {
        super(application);
        navigationCommands = new SingleLiveEvent<>();
    }

    public SingleLiveEvent<NavigationCommand> getNavigationCommands() { return navigationCommands; }

    public void navigate(NavDirections directions) {
        navigationCommands.postValue(new To(directions));
    }

    public void back() {
        navigationCommands.postValue(Back.getINSTANCE());
    }

    public void backTo(int destinationId) {
        navigationCommands.postValue(new BackTo(destinationId));
    }

    public void toRoot() {
        navigationCommands.postValue(ToRoot.getINSTANCE());
    }

    /**
     * Removes observers from single live event so not multiple observers on it.
     */
    public void resetNavigationCommandsSingleLiveEvent() {
        navigationCommands = new SingleLiveEvent<>();
    }
}
