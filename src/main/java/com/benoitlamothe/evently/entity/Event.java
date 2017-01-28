package com.benoitlamothe.evently.entity;

/**
 * Created by olivier on 2017-01-28.
 */

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Event {

    @SerializedName("name")
    public String name;

    public float latitude;

    public float longitude;

    public String location;

    public Date startTime;

    public Date endTime;

    public String category;

    public String description;

    public String link;

    public float price;


}
