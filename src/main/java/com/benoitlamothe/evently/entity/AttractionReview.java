package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by olivier on 2017-01-28.
 */
public class AttractionReview {

    @SerializedName("atractionId")
    public int attractionId;

    @SerializedName("rating")
    public int rating;

    @SerializedName("review")
    public String review;

    @SerializedName("date")
    public Date date;

    /*
        CREATE TABLE `Reviews` (
      `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
      `attraction_id` int(11) NOT NULL,
      `rating` int(11) NOT NULL,
      `review` text NOT NULL,
      `review_date` datetime NOT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */

    public PreparedStatement getSQLInsert(Connection connection) throws SQLException {
        PreparedStatement pst = connection.prepareStatement("INSERT INTO Attractions VALUES(?, ?, ?, ?)");
        int i = 1;
        pst.setObject(++i, this.attractionId);
        pst.setObject(++i, this.rating);
        pst.setObject(++i, this.review);
        pst.setObject(++i, this.date);

        return pst;
    }
}
