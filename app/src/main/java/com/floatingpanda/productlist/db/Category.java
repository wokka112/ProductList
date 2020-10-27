package com.floatingpanda.productlist.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//TODO write tests
/**
 * Represents a category of product in the store.
 */
@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    long id;

    String name;

    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this(0, name);
    }

    public void setId(long id) { this.id = id; }
    public long getId() { return id; }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
}
