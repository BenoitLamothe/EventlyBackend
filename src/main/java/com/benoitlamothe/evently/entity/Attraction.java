package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("reviews")
    public List<AttractionReview> reviews;

}
