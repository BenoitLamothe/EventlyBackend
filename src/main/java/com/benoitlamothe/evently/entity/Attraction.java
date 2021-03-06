package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;
import com.mysql.cj.api.jdbc.Statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class Attraction implements ILocalizable {
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

    @SerializedName("categories")
    public String categories;

    @SerializedName("mainCategory")
    public String mainCat;

    @SerializedName("duration")
    public int duration = 60;

    @SerializedName("rating")
    public Double rating;

    @SerializedName("reviews")
    public List<AttractionReview> reviews;

    public Integer getID() {
        return this.id;
    }

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
        PreparedStatement pst = connection.prepareStatement("INSERT INTO Attractions(`name`, loc_lat, loc_long, location, hours, hours_encoded, description, source_url, phone, website, price_range, categories, duration, rating) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
        pst.setObject(++i, this.categories);
        pst.setObject(++i, this.duration);
        pst.setObject(++i, this.rating);

        return pst;
    }

    public static List<Attraction> getAttractions(Connection connection) throws SQLException {
        PreparedStatement pst = connection.prepareStatement("SELECT * FROM Attractions");
        pst.execute();

        ResultSet rs = pst.getResultSet();
        LinkedList<Attraction> result = new LinkedList<Attraction>();
        while (rs.next()) {
            Attraction attr = new Attraction();
            attr.id = rs.getInt("id");
            attr.name = rs.getString("name");
            attr.latitude = rs.getFloat("loc_lat");
            attr.longitude = rs.getFloat("loc_long");
            attr.location = rs.getString("location");
            attr.hours = rs.getString("hours");
            attr.hoursShift = rs.getInt("hours_encoded");
            attr.description = rs.getString("description");
            attr.link = rs.getString("source_url");
            attr.phone = rs.getString("phone");
            attr.website = rs.getString("website");
            attr.priceRange = rs.getString("price_range");
            attr.categories = rs.getString("categories");
            attr.duration = rs.getInt("duration");
            attr.rating = rs.getDouble("rating");

            String[] cats = attr.categories.split(",");
            attr.mainCat = cats.length == 0 ? attr.categories : cats[0];

            result.add(attr);
        }

        return result;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public double getLat() {
        return this.latitude;
    }

    @Override
    public double getLong() {
        return this.longitude;
    }


    public boolean isRestaurant() {
        return categories.toLowerCase().contains("restaurant");
    }

    public boolean isHotel() {
        return categories.toLowerCase().contains("hotel");
    }

    public boolean isPark() {
        return categories.toLowerCase().contains("park");
    }

    public boolean isHeritage() {
        return categories.toLowerCase().contains("heritage");
    }
}
