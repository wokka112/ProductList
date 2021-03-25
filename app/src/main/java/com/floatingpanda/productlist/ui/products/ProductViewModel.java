package com.floatingpanda.productlist.ui.products;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.room.FtsOptions;

import com.floatingpanda.productlist.OrderByEnum;
import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Price;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductWithCategory;
import com.floatingpanda.productlist.repositories.CategoryRepository;
import com.floatingpanda.productlist.repositories.ProductRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    private LiveData<List<ProductWithCategory>> unsortedProductsWithCategories;
    private MediatorLiveData<List<ProductWithCategory>> sortedProductsWithCategories;
    private OrderByEnum currentOrdering;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);

        unsortedProductsWithCategories = productRepository.getAllProductsWithCategory();

        sortedProductsWithCategories = new MediatorLiveData<>();
        sortedProductsWithCategories.addSource(unsortedProductsWithCategories, new Observer<List<ProductWithCategory>>() {
            @Override
            public void onChanged(List<ProductWithCategory> productWithCategories) {
                // set sorted list as new mediator value
                sortedProductsWithCategories.setValue(sortProducts(productWithCategories));
            }
        });
    }

    // Used for testing purposes.
    public ProductViewModel(Application application, AppDatabase database) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);
        unsortedProductsWithCategories = productRepository.getAllProductsWithCategory();
    }

    public LiveData<List<ProductWithCategory>> getUnsortedProductsWithCategories() {
        return unsortedProductsWithCategories;
    }

    public LiveData<List<ProductWithCategory>> getProductsWithCategories() {
        return sortedProductsWithCategories;
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

    public void reorderProductList(OrderByEnum orderBy) {
        currentOrdering = orderBy;
        sortedProductsWithCategories.setValue(sortProducts(sortedProductsWithCategories.getValue()));
    }

    private List<ProductWithCategory> sortProducts(List<ProductWithCategory> productsWithCategories) {
        switch(currentOrdering) {
            case NAME:
                //sort by name asc
                Comparator<ProductWithCategory> byName =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product1.getProduct().getName().compareTo(product2.getProduct().getName());
                productsWithCategories.sort(byName);
                break;
            case NAME_INVERTED:
                //sort by name desc
                break;
            case BARCODE:
                //sort by barcode asc
                Comparator<ProductWithCategory> byBarcode =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product1.getProduct().getBarcode().compareTo(product2.getProduct().getBarcode());
                productsWithCategories.sort(byBarcode);
                break;
            case BARCODE_INVERTED:
                //sort by barcode desc
                break;
            case PRICE:
                //sort by price asc
                Comparator<ProductWithCategory> byPrice =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product1.getProduct().getPrice().compareTo(product2.getProduct().getPrice());
                productsWithCategories.sort(byPrice);
                break;
            case PRICE_INVERTED:
                //sort by price desc
                break;
            default:
                throw new IllegalArgumentException("Not a recognised enum.");
        }
        return productsWithCategories;
    }

    public void populateListWithAllProductsWithCategories() {
        currentOrdering = OrderByEnum.NAME;
        unsortedProductsWithCategories = productRepository.getAllProductsWithCategory();
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
        currentOrdering = OrderByEnum.NAME;
        unsortedProductsWithCategories = productRepository.getProductsWithCategoryByExactBarcode(barcode);
    }

    public void searchProducts(String barcode, String name, long categoryId, float lowerPrice, float higherPrice) {
        currentOrdering = OrderByEnum.NAME;
        unsortedProductsWithCategories = productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
    }

    public void searchProducts(String barcode, String name, long categoryId, Price lowerPrice, Price higherPrice) {
        currentOrdering = OrderByEnum.NAME;
        unsortedProductsWithCategories = productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);
    }
}
