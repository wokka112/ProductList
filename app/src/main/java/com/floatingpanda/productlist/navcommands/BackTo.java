package com.floatingpanda.productlist.navcommands;

import androidx.annotation.IdRes;

public class BackTo extends NavigationCommand {
    @IdRes
    private int destinationId;

    public BackTo(int destinationId) {
        this.destinationId = destinationId;
    }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }
}
