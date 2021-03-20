package com.floatingpanda.productlist.ui.products;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.floatingpanda.productlist.OrderByEnum;
import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Category;
import com.floatingpanda.productlist.db.Price;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductWithCategory;
import com.floatingpanda.productlist.repositories.CategoryRepository;
import com.floatingpanda.productlist.repositories.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private LiveData<List<ProductWithCategory>> productsWithCategories;
    private LiveData<List<ProductWithCategory>> searchedProductsWithCategories;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);
        productsWithCategories = productRepository.getAllProductsWithCategoryOrderedByName();
    }

    // Used for testing purposes.
    public ProductViewModel(Application application, AppDatabase database) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);
        productsWithCategories = productRepository.getAllProductsWithCategoryOrderedByName();
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategories() {
        return productsWithCategories;
    }

    public void addProductFromProductWithCategory(ProductWithCategory productWithCategory) {
        addProduct(productWithCategory.getProduct());
    }

    public void addProduct(Product product) {
        productRepository.addProduct(product);
    }

    public void editProductFromProductWithCategory(ProductWithCategory productWithCategory) {
        editProduct(productWithCategory.getProduct());
    }

    public void editProduct(Product product) {
        productRepository.editProduct(product);
    }

    public void deleteProductFromProductWithCategory(ProductWithCategory productWithCategory) {
        deleteProduct(productWithCategory.getProduct());
    }

    public void deleteProduct(Product product) {
        productRepository.deleteProduct(product);
    }

    public void deleteMultipleProductsFromProductsWithCategories(ProductWithCategory... productsWithCategories) {
        Product[] products = new Product[productsWithCategories.length];

        for (int i = 0; i < productsWithCategories.length; i++) {
            products[i] = productsWithCategories[i].getProduct();
        }

        deleteMultipleProducts(products);
    }

    public void deleteMultipleProductsFromProductsWithCategories(List<ProductWithCategory> productsWithCategories) {
        List<Product> products = new ArrayList<>();
        for (ProductWithCategory productWithCategory : productsWithCategories) {
            products.add(productWithCategory.getProduct());
        }

        deleteMultipleProducts(products);
    }

    public void deleteMultipleProducts(Product... products) {
        productRepository.deleteProducts(products);
    }

    public void deleteMultipleProducts(List<Product> products) {
        deleteMultipleProducts(products.toArray(new Product[products.size()]));
    }

    public void sortProductsByName() {
        productsWithCategories = productRepository.getAllProductsWithCategoryOrderedByName();
    }

    public void sortProductsByBarcode() {
        productsWithCategories = productRepository.getAllProductsWithCategoryOrderedByBarcode();
    }

    public void sortProductsByPrice() {
        productsWithCategories = productRepository.getAllProductsWithCategoryOrderedByBarcode();
    }

    /**
     * Searches the product list using exact matching on a string barcode, then sets the product
     * list to the search results.
     *
     * This is mainly for use with a barcode scanner where the exact barcode for a product can be
     * read by the device.
     * @param barcode a product's barcode in string form
     */
    public void searchProductsByBarcode(String barcode) {
        productsWithCategories = productRepository.getProductsWithCategoryByExactBarcode(barcode);
    }

    public void searchProducts(String barcode, String name, long categoryId, float lowerPrice, float higherPrice, OrderByEnum orderBy) {
        productsWithCategories = productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice, orderBy);
    }

    public void searchProducts(String barcode, String name, long categoryId, Price lowerPrice, Price higherPrice, OrderByEnum orderBy) {
        productsWithCategories = productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice, orderBy);
    }

    //TODO add in searches which order by barcode, name or price.
}
