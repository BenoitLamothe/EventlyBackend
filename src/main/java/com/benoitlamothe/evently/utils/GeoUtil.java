package com.benoitlamothe.evently.utils;

import com.benoitlamothe.evently.miners.TourismMiner;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

/**
 * Created by olivier on 2017-01-28.
 */
public class GeoUtil {
    public static GeoApiContext geoContext = new GeoApiContext().setApiKey("AIzaSyAYDNsIg7jX8OJYmhd81zpeHmG4aObW_2M");

    public static LatLong getLatLong(String query) {
        System.out.println("Query: " + query);
        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoContext, query).await();
            if (results.length > 0) {
                GeocodingResult first = results[0];
                return new LatLong((float) first.geometry.location.lat, (float) first.geometry.location.lng);
            } else {
                return new LatLong(0, 0);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class LatLong {
        public float latitude;
        public float longitude;

        public LatLong(float latitude, float longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
