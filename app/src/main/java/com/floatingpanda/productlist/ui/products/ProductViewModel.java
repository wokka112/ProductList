package com.floatingpanda.productlist.ui.products;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.floatingpanda.productlist.other.OrderByEnum;
import com.floatingpanda.productlist.db.AppDatabase;
import com.floatingpanda.productlist.db.Price;
import com.floatingpanda.productlist.db.Product;
import com.floatingpanda.productlist.db.ProductWithCategory;
import com.floatingpanda.productlist.repositories.CategoryRepository;
import com.floatingpanda.productlist.repositories.ProductRepository;
import com.floatingpanda.productlist.ui.base.BaseViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductViewModel extends BaseViewModel {
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    private LiveData<List<ProductWithCategory>> unsortedProductsWithCategories;
    // Used to sort the list of products in place, rather than querying database with different
    // ORDER BY values repeatedly
    private MediatorLiveData<List<ProductWithCategory>> sortedProductsWithCategories;

    // The sorted list should normally be ordered alphabetically and will be reset to this natural
    // ordering in certain circumstances, such as when repopulated.
    private final OrderByEnum naturalOrdering = OrderByEnum.NAME_ASC;
    private OrderByEnum currentOrdering;

    public ProductViewModel(Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        categoryRepository = new CategoryRepository(application);

        unsortedProductsWithCategories = productRepository.getAllProductsWithCategory();

        // Ordering always starts off alphabetically by name.
        currentOrdering = naturalOrdering;

        sortedProductsWithCategories = new MediatorLiveData<>();
        addUnsortedProductsWithCategoriesToSortedProductsMediator(productRepository.getAllProductsWithCategory());
    }

    // Used for testing purposes.
    public ProductViewModel(Application application, AppDatabase database) {
        super(application);
        productRepository = new ProductRepository(database);
        categoryRepository = new CategoryRepository(database);
        unsortedProductsWithCategories = productRepository.getAllProductsWithCategory();

        // Ordering always starts off alphabetically by name.
        currentOrdering = naturalOrdering;

        sortedProductsWithCategories = new MediatorLiveData<>();
        addUnsortedProductsWithCategoriesToSortedProductsMediator(productRepository.getAllProductsWithCategory());
    }

    public OrderByEnum getNaturalOrdering() { return naturalOrdering; }

    public OrderByEnum getCurrentOrdering() { return currentOrdering; }

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

    public void reorderProductList(OrderByEnum orderBy) throws NullPointerException {
        currentOrdering = orderBy;
        List<ProductWithCategory> productsWithCategories = sortedProductsWithCategories.getValue();

        if (productsWithCategories == null) {
            throw new NullPointerException("reorderProductList() failed because LiveData is set to null");
            // Could I create a thread, wait for the list to be non-null and then sort it?
            // Create thread with runnable
            //      sortedProductsWithCategories.wait(30000). (30 second timeout)
        }

        Log.w("ProductViewModel", "Sorted Products value before sorting: " + sortedProductsWithCategories.getValue());
        sortedProductsWithCategories.setValue(sortProducts(productsWithCategories));
    }

    public void populateListWithAllProductsWithCategories() {
        // Remove the old unsorted list from the sorted list mediator
        removeUnsortedProductsWithCategoriesFromSortedProductsMediator();

        // Add the new unsorted list to the sorted list mediator
        addUnsortedProductsWithCategoriesToSortedProductsMediator(productRepository.getAllProductsWithCategory());
    }

    /**
     * Searches the product list using exact matching on a string barcode, then sets the product
     * list to the search results.
     *
     * This is mainly for use with a barcode scanner where the exact barcode for a product can be
     * read by the device.
     * @param barcode a product's barcode in string form
     */
    public void searchProductsWithCategoryByBarcode(String barcode) {
        // Remove the old unsorted list from the sorted list mediator
        removeUnsortedProductsWithCategoriesFromSortedProductsMediator();

        LiveData<List<ProductWithCategory>> searchedProducts = productRepository.getProductsWithCategoryByExactBarcode(barcode);

        // Add the new unsorted list to the sorted list mediator
        addUnsortedProductsWithCategoriesToSortedProductsMediator(searchedProducts);
    }

    public void filterProductsWithCategoryByCategoryId(long categoryId) {
        // Remove the old unsorted list from the sorted list mediator
        removeUnsortedProductsWithCategoriesFromSortedProductsMediator();

        LiveData<List<ProductWithCategory>> filteredProducts = productRepository.getProductsWithCategoryByCategoryId(categoryId);

        // Add the new unsorted list to the sorted list mediator
        addUnsortedProductsWithCategoriesToSortedProductsMediator(filteredProducts);
    }

    public void searchProductsWithCategory(String barcode, String name, long categoryId, float lowerPrice, float higherPrice) {
        // Remove the old unsorted list from the sorted list mediator
        removeUnsortedProductsWithCategoriesFromSortedProductsMediator();

        LiveData<List<ProductWithCategory>> searchedProducts = productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);

        // Add the new unsorted list to the sorted list mediator
        addUnsortedProductsWithCategoriesToSortedProductsMediator(searchedProducts);
    }

    public void searchProductsWithCategory(String barcode, String name, long categoryId, Price lowerPrice, Price higherPrice) {
        // Remove the old unsorted list from the sorted list mediator
        removeUnsortedProductsWithCategoriesFromSortedProductsMediator();

        LiveData<List<ProductWithCategory>> searchedProducts = productRepository.searchProductsWithCategory(barcode, name, categoryId, lowerPrice, higherPrice);

        // Add the new unsorted list to the sorted list mediator
        addUnsortedProductsWithCategoriesToSortedProductsMediator(searchedProducts);
    }

    // ------------------------------------ PRIVATE METHODS ------------------------------------ //

    private void addUnsortedProductsWithCategoriesToSortedProductsMediator(LiveData<List<ProductWithCategory>> unsortedProductsWithCategories) {
        // Reset current sorted list ordering to the natural ordering
        currentOrdering = naturalOrdering;

        // Keep track of the new unsorted list source for the mediator so we can remove it later
        this.unsortedProductsWithCategories = unsortedProductsWithCategories;

        // Add the new unsorted list to the mediator and set up the sorting behaviour
        sortedProductsWithCategories.addSource(this.unsortedProductsWithCategories, new Observer<List<ProductWithCategory>>() {
            @Override
            public void onChanged(List<ProductWithCategory> productWithCategories) {
                // set sorted list as new mediator value
                sortedProductsWithCategories.setValue(sortProducts(productWithCategories));
            }
        });
    }

    private void removeUnsortedProductsWithCategoriesFromSortedProductsMediator() {
        sortedProductsWithCategories.removeSource(unsortedProductsWithCategories);
    }

    private List<ProductWithCategory> sortProducts(List<ProductWithCategory> productsWithCategories) {
        switch(currentOrdering) {
            case NAME_ASC:
                //sort by name asc
                Comparator<ProductWithCategory> byNameAsc =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product1.getProduct().getName().compareTo(product2.getProduct().getName());
                productsWithCategories.sort(byNameAsc);
                break;
            case NAME_DESC:
                Comparator<ProductWithCategory> byNameDesc =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product2.getProduct().getName().compareTo(product1.getProduct().getName());
                productsWithCategories.sort(byNameDesc);
                //sort by name desc
                break;
            case BARCODE_ASC:
                //sort by barcode asc
                Comparator<ProductWithCategory> byBarcodeAsc =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product1.getProduct().getBarcode().compareTo(product2.getProduct().getBarcode());
                productsWithCategories.sort(byBarcodeAsc);
                break;
            case BARCODE_DESC:
                //sort by barcode desc
                Comparator<ProductWithCategory> byBarcodeDesc =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product2.getProduct().getBarcode().compareTo(product1.getProduct().getBarcode());
                productsWithCategories.sort(byBarcodeDesc);
                break;
            case PRICE_ASC:
                //sort by price asc
                Comparator<ProductWithCategory> byPriceAsc =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product1.getProduct().getPrice().compareTo(product2.getProduct().getPrice());
                productsWithCategories.sort(byPriceAsc);
                break;
            case PRICE_DESC:
                //sort by price desc
                Comparator<ProductWithCategory> byPriceDesc =
                        (ProductWithCategory product1, ProductWithCategory product2) -> product2.getProduct().getPrice().compareTo(product1.getProduct().getPrice());
                productsWithCategories.sort(byPriceDesc);
                break;
            default:
                throw new IllegalArgumentException("Not a recognised enum.");
        }
        return productsWithCategories;
    }
}
