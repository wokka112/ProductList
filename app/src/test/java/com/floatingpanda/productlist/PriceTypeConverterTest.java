package com.floatingpanda.productlist;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriceTypeConverterTest {
    public static final Price PRODUCT_1_PRICE = new Price(5, 99);
    public static final Price PRODUCT_2_PRICE = new Price(0, 9);

    public static final int PRODUCT_1_PRICE_INT = 599;
    public static final int PRODUCT_2_PRICE_INT = 9;

    @Test
    public void turnPriceStringIntoPrice() {
        Price product1Price = PriceTypeConverter.fromPriceInt(PRODUCT_1_PRICE_INT);

        assertEquals(PRODUCT_1_PRICE, product1Price);

        Price product2Price = PriceTypeConverter.fromPriceInt(PRODUCT_2_PRICE_INT);

        assertEquals(PRODUCT_2_PRICE, product2Price);
    }

    @Test
    public void turnPriceIntoPriceString() {
        int product1PriceInt = PriceTypeConverter.toPriceInt(PRODUCT_1_PRICE);

        assertEquals(PRODUCT_1_PRICE_INT, product1PriceInt);

        int product2PriceInt = PriceTypeConverter.toPriceInt(PRODUCT_2_PRICE);

        assertEquals(PRODUCT_2_PRICE_INT, product2PriceInt);
    }

    /*
    @TypeConverter
    public static Price fromPriceString(String priceString) {
        String[] strings = priceString.split(".");

        //First string is pounds
        int pounds = Integer.parseInt(strings[0]);

        //Second string is pence
        int pence = Integer.parseInt(strings[1]);

        return new Price(pounds, pence);
    }

    @TypeConverter
    public static String toPriceString(Price price) {
        String priceString = Integer.toString(price.getPounds()) + ".";

        if (price.getPence() < 10) {
            priceString += "0";
        }

        priceString += Integer.toString(price.getPence());

        return priceString;
    }
     */

}
