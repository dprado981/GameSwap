package com.codepath.gameswap.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class MapUtils {

    public static final int LOCATION_PERMISSION_CODE = 100;

    public static LatLng adjustedLatLng(double latitude, double longitude, int radius) {
        double metersToDegrees = 111111;
        Random random = new Random();
        int precisionDecimals = (int) Math.log10(radius);
        int powerOfTen = (int) Math.pow(10, precisionDecimals);
        int bound = 2 * powerOfTen + 1;
        double latAdjustment = radius / metersToDegrees * (random.nextInt(bound) - powerOfTen) / powerOfTen;
        double longAdjustment = radius / metersToDegrees * (random.nextInt(bound) - powerOfTen) / powerOfTen;
        return new LatLng(latitude + latAdjustment, longitude + longAdjustment);
    }
}
