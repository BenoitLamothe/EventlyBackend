package com.benoitlamothe.evently.entity;

/**
 * Created by olivier on 2017-01-28.
 */

import com.google.gson.annotations.SerializedName;
import sun.tools.tree.Statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("lat")
    public float latitude;

    @SerializedName("long")
    public float longitude;

    @SerializedName("location")
    public String location;

    @SerializedName("startTime")
    public Date startTime;

    @SerializedName("endTime")
    public Date endTime;

    @SerializedName("category")
    public String category;

    @SerializedName("description")
    public String description;

    @SerializedName("link")
    public String link;

    @SerializedName("price")
    public float price;

    @SerializedName("priceDisplay")
    public String priceDisplay;

    @SerializedName("images")
    public List<String> imageSources;

    public PreparedStatement getSQLInsert(Connection connection) throws SQLException {
        PreparedStatement pst = connection.prepareStatement("INSERT INTO Events(`name`, loc_lat, loc_long, location, startDatetime, endDatetime, category, description, website, price_range, price_display) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS);
        int i = 0;
        pst.setObject(++i, this.name);
        pst.setObject(++i, this.latitude);
        pst.setObject(++i, this.longitude);
        pst.setObject(++i, this.location);
        pst.setObject(++i, this.startTime);
        pst.setObject(++i, this.endTime);
        pst.setObject(++i, this.category);
        pst.setObject(++i, this.description);
        pst.setObject(++i, this.link);
        pst.setObject(++i, this.price);
        pst.setObject(++i, this.priceDisplay);

        return pst;
    }



    public static List<Event> getAll(Connection conn) throws SQLException {
        PreparedStatement pst = conn.prepareStatement("SELECT * FROM Events INNER JOIN Assets ON Events.id = Assets.event_id");

        ResultSet rst = pst.executeQuery();
        ArrayList<Event> events = new ArrayList<>();
        while(rst.next()) {
            boolean duplicate = false;
            for(Event e : events) {
                if(e.id == rst.getInt("id")) {
                    duplicate = true;
                    e.imageSources.add(rst.getString("url"));
                    break;
                }
            }

            if(duplicate) {
                continue;
            }

            Event e = new Event();
            e.id = rst.getInt("id");
            e.name = rst.getString("name");
            e.latitude = rst.getFloat("loc_lat");
            e.longitude = rst.getFloat("loc_long");
            e.location = rst.getString("location");
            e.startTime = rst.getDate("startDateTime");
            e.endTime = rst.getDate("endDateTime");
            e.category = rst.getString("category");
            e.description = rst.getString("description");
            e.link = rst.getString("website");
            e.price = rst.getFloat("price_range");
            e.priceDisplay = rst.getString("price_display");

            e.imageSources = new ArrayList<String>(){{
                add(rst.getString("url"));
            }};

            events.add(e);
        }

        return events;
    }
}
