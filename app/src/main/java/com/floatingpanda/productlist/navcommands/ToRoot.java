package com.floatingpanda.productlist.navcommands;

public final class ToRoot extends NavigationCommand {
    private static ToRoot INSTANCE;

    private ToRoot() {

    }

    public static ToRoot getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new ToRoot();
        }

        return INSTANCE;
    }
}
