package com.benoitlamothe.evently.entity;

/**
 * Created by olivier on 2017-01-28.
 */

import com.google.gson.annotations.SerializedName;

import java.util.Date;

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


}
