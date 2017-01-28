package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Asset;
import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.utils.CloudUtils;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class YelpMiner {
    private static String OAUTH_TOKEN = "SP58E1JNjpP1xmdnfak3zcHUa1HZx9bmYWZEzdNtRhCGiqRoDcicI0UicfAxQCVAjR5gBBHR9vU41168O54DyTlDU8_PKGjFVG6ocK6MvqR5gdr2-uALb9iEWsyMWHYx";
    private static int LIMIT = 10;

    private static String URL_SEARCH_API = "https://api.yelp.com/v3/businesses/search";
    private static String URL_BUSINESS_API = "https://api.yelp.com/v3/businesses/";

    private static final HikariConfig config = new HikariConfig("/hikari.properties");
    private static final HikariDataSource ds = new HikariDataSource(config);
    private static Connection dbConn;

    static {
        Unirest.setDefaultHeader("Authorization", "Bearer " + OAUTH_TOKEN);
        try {
            dbConn = ds.getConnection();
            dbConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void minePerLocation(String location) throws UnirestException, SQLException, IOException {
        minePerLocation(location, LIMIT, 0);
    }

    public void minePerLocation(String location, int limit, int offset) throws UnirestException, SQLException, IOException {
        String searchUrl = URL_SEARCH_API + "?location=" + location + "&limit=" + limit + "&offset=" + offset;
        System.out.println(searchUrl);
        HttpResponse<JsonNode> resultByLocation =
                Unirest.get(searchUrl).asJson();
        if(resultByLocation.getStatus() != 200) { throw new RuntimeException("REST HTTP error" + resultByLocation.getStatusText()); }

        JSONObject searchResult = resultByLocation.getBody().getObject();
        int totalResult = searchResult.getInt("total");
        System.out.println(String.format("Found %d entries from yelp", searchResult.getInt("total")));

        JSONArray businesses = searchResult.getJSONArray("businesses");
        for(int i = 0; i < businesses.length(); i++) {
            JSONObject business = businesses.getJSONObject(i);
            JSONObject businessDetail = Unirest.get(URL_BUSINESS_API + business.getString("id")).asJson().getBody().getObject();

            Attraction currentAttraction = new Attraction();
            currentAttraction.name = business.getString("name");
            currentAttraction.location = String.format("%s, %s, %s %s, %s",
                    business.getJSONObject("location").getString("address1"),
                    business.getJSONObject("location").getString("city"),
                    business.getJSONObject("location").getString("state"),
                    business.getJSONObject("location").getString("zip_code"),
                    business.getJSONObject("location").getString("country"));
            currentAttraction.latitude = (float) business.getJSONObject("coordinates").getDouble("latitude");
            currentAttraction.longitude = (float) business.getJSONObject("coordinates").getDouble("longitude");
            currentAttraction.hoursShift = businessDetail.has("hours") ? this.determineHoursShift(businessDetail.getJSONArray("hours")) : 0;
            currentAttraction.description = "";
            currentAttraction.link = businessDetail.getString("url");
            currentAttraction.phone = businessDetail.getString("phone");
            currentAttraction.website = "";
            currentAttraction.priceRange = business.has("price") ? business.getString("price") : null;
            currentAttraction.reviewStars = (float) business.getDouble("rating");
            PreparedStatement pstmtAttraction = currentAttraction.getSQLInsert(dbConn);
            pstmtAttraction.executeUpdate();

            LinkedList<String> photos = new LinkedList<String>();
            photos.add(business.getString("image_url"));
            for(Object url : businessDetail.getJSONArray("photos")) {
                photos.add((String) url);
            }

            LinkedList<Asset> assets = new LinkedList<Asset>();
            for(String url : photos) {
                ResultSet keys = pstmtAttraction.getGeneratedKeys();
                keys.next();

                Asset ass = new Asset();
                System.out.println("Downloaded " + url);
                ass.url = CloudUtils.STORAGE_URI + CloudUtils.downloadToBucket(url).getName();
                ass.type = "Image";
                ass.attactionId = keys.getInt(1);
                ass.getSQLInsert(dbConn).executeUpdate();
            }

            dbConn.commit();
            System.out.println("Done parsing " + i + " - " + currentAttraction.name);
            offset++;
        }

        if(offset + limit < totalResult) {
            minePerLocation(location, limit, offset);
        }
    }

    private int determineHoursShift(JSONArray hours) {
        int finalShift = 0b0000000;
        for(int i = 0; i < hours.length(); i++) {
            JSONObject hour = hours.getJSONObject(i);
            JSONArray hoursOpen = hour.getJSONArray("open");
            for(int j = 0; j < hoursOpen.length(); j++) {
                JSONObject hourOpen = hoursOpen.getJSONObject(j);
                int currentShift = 1 << hourOpen.getInt("day");
                finalShift = finalShift | currentShift;
            }

        }

        return finalShift;
    }

    public static void main(String[] args) throws UnirestException, SQLException, IOException {
        YelpMiner miner = new YelpMiner();
        miner.minePerLocation("Shawinigan");
        //CloudUtils.downloadToBucket("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Pepe_at_Yankee_Stadium.jpg/262px-Pepe_at_Yankee_Stadium.jpg");
    }
}
