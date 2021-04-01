package com.floatingpanda.productlist.navcommands;

import androidx.navigation.NavDirections;

public class To extends NavigationCommand {
    private NavDirections navDirections;

    public To(NavDirections navDirections) {
        this.navDirections = navDirections;
    }

    public NavDirections getNavDirections() { return navDirections; }
    public void setNavDirections(NavDirections navDirections) { this.navDirections = navDirections; }
}
