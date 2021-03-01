package com.floatingpanda.productlist.db;

import androidx.room.TypeConverter;

// Price is stored in the DB as an integer which is 100 times larger than the price in pounds.
// So £9.99 should be stored as 999.
public class PriceTypeConverter {
    @TypeConverter
    public static Price fromPriceInt(int priceInt) {
        int pounds = priceInt / 100;
        int pence = priceInt % 100;

        return new Price(pounds, pence);
    }

    @TypeConverter
    public static int toPriceInt(Price price) {
        return (price.getPounds() * 100) + price.getPence();
    }
}
