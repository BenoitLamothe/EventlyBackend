package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Asset;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.utils.CloudUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jeremiep on 2017-01-29.
 */
public class EventbriteMiner {
    private static String OAUTH_TOKEN = "53QM7WSQUWPCMTHHPEZ4";
    private static final String SEARCH_URL = "https://www.eventbriteapi.com/v3/events/search/";
    private static final String VENUE_URL = "https://www.eventbriteapi.com/v3/venues/";
    private static final String CATEGORY_URL = "https://www.eventbriteapi.com/v3/categories/";

    private static final HikariConfig config = new HikariConfig("/hikari.properties");
    private static final HikariDataSource ds = new HikariDataSource(config);
    private static Connection dbConn;


    static {
        try {
            dbConn = ds.getConnection();
            dbConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Unirest.setDefaultHeader("Authorization", "Bearer " + OAUTH_TOKEN);
    }

    public void mine() throws UnirestException, SQLException, IOException {
        HttpResponse<JsonNode> eventSearch = Unirest.get(SEARCH_URL + "?location.address=Shawinigan,%20Canada").asJson();
        JsonNode events = eventSearch.getBody();
        JSONArray eventsArr = events.getObject().getJSONArray("events");

        for(int i = 0; i < eventsArr.length(); i++) {
            JSONObject eventObj = eventsArr.getJSONObject(i);
            JSONObject eventVenue = Unirest.get(VENUE_URL + eventObj.getString("venue_id") + "/?venue=" + eventObj.getString("venue_id")).asJson().getBody().getObject();
            if(!eventObj.has("category_id")) { System.out.println("skip"); continue; }
            Object categoryId = eventObj.get("category_id");
            JSONObject category = Unirest.get(CATEGORY_URL + categoryId.toString()).asJson().getBody().getObject();

            Event evt = new Event();
            if(!eventObj.has("name")) { continue; }
            evt.name = eventObj.getJSONObject("name").getString("text");
            evt.startTime = DateTime.parse(eventObj.getJSONObject("start").getString("local"));
            evt.endTime = DateTime.parse(eventObj.getJSONObject("end").getString("local"));
            evt.location = eventVenue.getJSONObject("address").getString("localized_address_display");

            if(eventObj.has("description")) {
                if(eventObj.getJSONObject("description").has("text")) {
                    Object t = eventObj.getJSONObject("description").get("text");
                    evt.description = t.toString();
                } else if (eventObj.getJSONObject("description").has("html")) {
                    Object t = eventObj.getJSONObject("description").get("html");
                    evt.description = t.toString();
                }
            }
            evt.latitude = (float) eventVenue.getJSONObject("address").getDouble("latitude");
            evt.longitude = (float) eventVenue.getJSONObject("address").getDouble("longitude");
            evt.link = eventObj.getString("url");
            if(!category.has("name")) { continue; }
            evt.category = category.getString("name");

            PreparedStatement stmt = evt.getSQLInsert(dbConn);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();

            rs.next();
            try {
                if(eventObj.has("logo") && eventObj.getJSONObject("logo").has("original")) {
                    String url = eventObj.getJSONObject("logo").getJSONObject("original").getString("url");
                    String cdnUrl = CloudUtils.STORAGE_URI + CloudUtils.downloadToBucket(url).getName();

                    System.out.println("Downloaded " + cdnUrl);

                    Asset asset = new Asset();
                    asset.url = cdnUrl;
                    asset.type = "Image";
                    asset.eventId = rs.getInt(1);
                    asset.getSQLInsert(dbConn).executeUpdate();
                }
            } catch (Throwable e) {}
            System.out.println("Fetched " + i);
        }

        dbConn.commit();
        System.out.println("Finished scrapping eventbrite");
    }

    public static void main(String[] args) throws UnirestException, IOException, SQLException {
        EventbriteMiner eventBrite = new EventbriteMiner();
        eventBrite.mine();
    }
}
