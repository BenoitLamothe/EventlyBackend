package com.benoitlamothe.evently.entity;

/**
 * Created by olivier on 2017-01-28.
 */

import com.google.gson.annotations.SerializedName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {

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

    /*
        CREATE TABLE `Events` (
          `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
          `name` varchar(254) NOT NULL DEFAULT '',
          `loc_lat` float NOT NULL,
          `loc_long` float NOT NULL,
          `location` varchar(254) NOT NULL DEFAULT '',
          `startDatetime` datetime NOT NULL,
          `endDatetime` datetime DEFAULT NULL,
          `category` varchar(254) NOT NULL DEFAULT '',
          `description` text,
          `website` varchar(254) DEFAULT NULL,
          `price_range` float DEFAULT NULL,
          PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    */

    public PreparedStatement getSQLInsert(Connection connection) throws SQLException {
        PreparedStatement pst = connection.prepareStatement("INSERT INTO Events(`name`, loc_lat, loc_long, location, startDatetime, endDatetime, category, description, website, price_range, price_display) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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

    static List<Event> fromResultSet(ResultSet resultSet) {
        return new ArrayList<>();
    }
}
