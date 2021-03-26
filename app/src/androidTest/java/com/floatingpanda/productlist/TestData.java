package com.floatingpanda.productlist;

import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.Price;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductWithCategory;

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
    public static final Price PRODUCT_5_PRICE = new Price(20, 95);

    // Product 1 and Product 3 should be the only products with barcodes starting 12345.
    // Only products 1 and 2 should have category id 1
    public static final Product PRODUCT_1 = new Product(4, "Taps", "1234567890123", PRODUCT_1_PRICE, 1, "None");
    public static final Product PRODUCT_2 = new Product(5, "Sink",  "3210987654321",PRODUCT_2_PRICE, 1, "Notesssss");
    public static final Product PRODUCT_3 = new Product(6, "Pillows",  "1234509876109",PRODUCT_3_PRICE, 2, "Flippledee");
    public static final Product PRODUCT_4 = new Product(7, "Sweets",  "0897621453857",PRODUCT_4_PRICE, 0, "Floopledoo");
    public static final Product PRODUCT_5 = new Product(8, "Something",  "3210987654321",PRODUCT_5_PRICE, 0, "Amazing");

    public static final List<Product> PRODUCTS = Arrays.asList(PRODUCT_1, PRODUCT_2, PRODUCT_3, PRODUCT_4, PRODUCT_5);

    public static final ProductWithCategory PRODUCT_1_WITH_CATEGORY = new ProductWithCategory(PRODUCT_1, CATEGORY_1);
    public static final ProductWithCategory PRODUCT_2_WITH_CATEGORY = new ProductWithCategory(PRODUCT_2, CATEGORY_1);
    public static final ProductWithCategory PRODUCT_3_WITH_CATEGORY = new ProductWithCategory(PRODUCT_3, CATEGORY_2);
    public static final ProductWithCategory PRODUCT_4_WITH_CATEGORY = new ProductWithCategory(PRODUCT_4, null);
    public static final ProductWithCategory PRODUCT_5_WITH_CATEGORY = new ProductWithCategory(PRODUCT_5, null);

    public static final Price PRODUCT_TO_ADD_1_PRICE = new Price(7, 01);
    public static final Product PRODUCT_TO_ADD_1 = new Product(9, "Entirely new item", "3210987654321", PRODUCT_TO_ADD_1_PRICE, 1, "FLIPPER");

    public static final Price PRODUCT_TO_ADD_PRICE_2 = new Price(8, 74);
    public static final Product PRODUCT_TO_ADD_2 = new Product(10, "Pepper", "1234576123009", PRODUCT_TO_ADD_PRICE_2, 1, "None");
}
