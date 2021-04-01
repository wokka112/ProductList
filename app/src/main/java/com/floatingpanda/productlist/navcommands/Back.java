package com.floatingpanda.productlist.navcommands;

public final class Back extends NavigationCommand {
    private static Back INSTANCE;

    private Back() {

    }

    public static Back getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Back();
        }

        return INSTANCE;
    }
}
