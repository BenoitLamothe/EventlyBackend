package com.benoitlamothe.evently.utils;

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

    /*
 * Calculate distance between two points in latitude and longitude taking
 * into account height difference. If you are not interested in height
 * difference pass 0.0. Uses Haversine method as its base.
 *
 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
 * el2 End altitude in meters
 * @returns Distance in Meters
 */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
