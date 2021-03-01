package com.floatingpanda.productlist.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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

    // Describes a price in terms of British pounds and pence.
    // Price is stored in the DB as an integer which is 100 times larger than the price in pounds.
    // So £9.99 should be stored as 999.
    @NonNull
    private Price price;

    @ColumnInfo(name = "category_id", defaultValue = "0")
    @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "category_id", onDelete = ForeignKey.SET_DEFAULT)
    private long categoryId;

    @Nullable
    private String notes;

    public Product(long id, String barcode, String name, Price price, long categoryId, String notes) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.notes = notes;
    }

    //TODO go over these creation methods and remove unnecessary ones
    @Ignore
    public Product(String barcode, String name, Price price, long categoryId, String notes) {
        this(0, barcode, name, price, categoryId, notes);
    }

    @Ignore
    public Product(String barcode, String name, Price price, long categoryId) {
        this(0, barcode, name, price, categoryId, "None");
    }

    @Ignore
    public Product(String barcode, String name, Price price, String notes) {
        this(0, barcode, name, price, 0, notes);
    }

    @Ignore
    public Product(String barcode, String name, Price price) {
        this(0, barcode, name, price, 0, "None");
    }

    @Ignore
    public Product(String name, Price price, long categoryId, String notes) {
        this(0, null, name, price, categoryId, notes);
    }

    @Ignore
    public Product(String name, Price price, long categoryId) {
        this(0, null, name, price, categoryId, "None");
    }

    @Ignore
    public Product(String name, Price price, String notes) {
        this(0, null, name, price, 0, notes);
    }

    @Ignore
    public Product(String name, Price price) {
        this(0, null, name, price, 0, "None");
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getBarcode() { return barcode; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public void setPrice(Price price) { this.price = price; }
    public Price getPrice() { return price; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId;}
    public long getCategoryId() { return categoryId; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getNotes() { return notes; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Product product = (Product) obj;

        return (product.getBarcode().equals(this.getBarcode())
                && product.getName().equals(this.getName())
                && product.getPrice().equals(this.getPrice())
                && product.getCategoryId() == this.getCategoryId()
                && product.getNotes().equals(this.getNotes()));
    }
}
