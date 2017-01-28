package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;
import com.mysql.cj.api.jdbc.Statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class Attraction {

    @SerializedName("name")
    public String name;

    @SerializedName("lat")
    public float latitude;

    @SerializedName("long")
    public float longitude;

    @SerializedName("location")
    public String location;

    @SerializedName("hours")
    public String hours;

    @SerializedName("hoursShift")
    public int hoursShift;

    @SerializedName("description")
    public String description;

    @SerializedName("link")
    public String link;

    @SerializedName("phone")
    public String phone;

    @SerializedName("website")
    public String website;

    @SerializedName("reviewStars")
    public float reviewStars;

    @SerializedName("priceRange")
    public String priceRange;

    @SerializedName("reviews")
    public List<AttractionReview> reviews;

    /*
        CREATE TABLE `Attractions` (
      `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
      `name` varchar(254) NOT NULL DEFAULT '',
      `loc_lat` float NOT NULL,
      `loc_long` float NOT NULL,
      `location` varchar(254) NOT NULL DEFAULT '',
      `hours` text,
      `hours_encoded` int(11) DEFAULT NULL,
      `description` text,
      `source_url` varchar(254) NOT NULL DEFAULT '',
      `phone` varchar(254) DEFAULT NULL,
      `website` varchar(254) NOT NULL DEFAULT '',
      `website` int(11) NULL DEFAULT ''
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

     */
    public PreparedStatement getSQLInsert(Connection connection) throws SQLException {
        PreparedStatement pst = connection.prepareStatement("INSERT INTO Attractions(`name`, loc_lat, loc_long, location, hours, hours_encoded, description, source_url, phone, website, price_range) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        int i = 0;
        pst.setObject(++i, this.name);
        pst.setObject(++i, this.latitude);
        pst.setObject(++i, this.longitude);
        pst.setObject(++i, this.location);
        pst.setObject(++i, this.hours);
        pst.setObject(++i, this.hoursShift);
        pst.setObject(++i, this.description);
        pst.setObject(++i, this.link);
        pst.setObject(++i, this.phone);
        pst.setObject(++i, this.website);
        pst.setObject(++i, this.priceRange);

        return pst;
    }
}
