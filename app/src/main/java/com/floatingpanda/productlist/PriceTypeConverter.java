package com.floatingpanda.productlist;

import androidx.room.TypeConverter;

//TODO write tests
public class PriceTypeConverter {
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
}
