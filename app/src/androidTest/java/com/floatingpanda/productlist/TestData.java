package com.floatingpanda.productlist;

import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.Product;

import java.util.Arrays;
import java.util.List;

public class TestData {
    public static final Category CATEGORY_1 = new Category(1, "Bathroom");
    public static final Category CATEGORY_2 = new Category(2, "Bedding");
    public static final Category CATEGORY_3 = new Category(3, "Car");

    public static final List<Category> CATEGORIES = Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3);

    public static final Product PRODUCT_1 = new Product(4, "1234567890123", "Taps", 5.99f, 1, "None");
    public static final Product PRODUCT_2 = new Product(5, "3210987654321", "Sink", 10.99f, 1, "Notesssss");
    public static final Product PRODUCT_3 = new Product(6, "1234509876109", "Pillows", 18.99f, 2, "Flippledee");
    public static final Product PRODUCT_4 = new Product(7, "0987612345875", "Sweets", 0.99f, 0, "Floopledoo");

    public static final List<Product> PRODUCTS = Arrays.asList(PRODUCT_1, PRODUCT_2, PRODUCT_3, PRODUCT_4);

}
