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

    public static final Price PRODUCT_1_PRICE = new Price(5, 99);
    public static final Price PRODUCT_2_PRICE = new Price(10, 99);
    public static final Price PRODUCT_3_PRICE = new Price(18, 99);
    public static final Price PRODUCT_4_PRICE = new Price(0, 9);

    // Each barcode should be unique for these products.
    // Product 1 and Product 3 should be the only products with barcodes starting 12345.
    // Only products 1 and 2 should have category id 1
    public static final Product PRODUCT_1 = new Product(4, "1234567890123", "Taps", PRODUCT_1_PRICE, 1, "None");
    public static final Product PRODUCT_2 = new Product(5, "3210987654321", "Sink", PRODUCT_2_PRICE, 1, "Notesssss");
    public static final Product PRODUCT_3 = new Product(6, "1234509876109", "Pillows", PRODUCT_3_PRICE, 2, "Flippledee");
    public static final Product PRODUCT_4 = new Product(7, "0897621453857", "Sweets", PRODUCT_4_PRICE, 0, "Floopledoo");

    public static final List<Product> PRODUCTS = Arrays.asList(PRODUCT_1, PRODUCT_2, PRODUCT_3, PRODUCT_4);
}
