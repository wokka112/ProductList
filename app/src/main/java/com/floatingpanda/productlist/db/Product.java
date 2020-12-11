package com.floatingpanda.productlist.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

//TODO write tests
/**
 * Represents a product in the store. A product may be without a barcode. A product may also have no
 * category it belongs to.
 */
@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private String barcode;

    @NonNull
    private String name;

    private float price;

    @ColumnInfo(name = "category_id", defaultValue = "0")
    @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "category_id", onDelete = ForeignKey.SET_DEFAULT)
    private long categoryId;

    public Product(long id, String barcode, String name, float price, long categoryId) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
    }

    @Ignore
    public Product(String barcode, String name, float price, long categoryId) {
        this(0, barcode, name, price, categoryId);
    }

    @Ignore
    public Product(String barcode, String name, float price) {
        this(0, barcode, name, price, 0);
    }

    @Ignore
    public Product(String name, float price, long categoryId) {
        this(0, null, name, price, categoryId);
    }

    @Ignore
    public Product(String name, float price) {
        this(0, null, name, price, 0);
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getBarcode() { return barcode; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setPrice(float price) { this.price = price; }
    public float getPrice() { return price; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId;}
    public long getCategoryId() { return categoryId; }
}
