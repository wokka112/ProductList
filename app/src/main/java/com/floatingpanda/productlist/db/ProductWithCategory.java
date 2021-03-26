package com.floatingpanda.productlist.db;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

public class ProductWithCategory {
    @Embedded
    public Product product;
    @Relation(
            parentColumn = "category_id",
            entityColumn = "id"
    )
    public Category category;

    public ProductWithCategory(Product product, Category category) {
        this.product = product;
        this.category = category;
    }

    public void setProduct(Product product) { this.product = product; }
    public Product getProduct() { return product; }
    public void setCategory(Category category) { this.category = category; }
    public Category getCategory() { return category; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ProductWithCategory otherProductWithCategory = (ProductWithCategory) obj;

        return otherProductWithCategory.getProduct().equals(this.getProduct())
                && otherProductWithCategory.getCategory().equals(this.getCategory());
    }

    @Override
    public int hashCode() {
        return getProduct().hashCode();
    }
}
