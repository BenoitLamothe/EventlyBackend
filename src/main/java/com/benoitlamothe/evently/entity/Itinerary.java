package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class Itinerary {
    @SerializedName("event")
    public Event event;

    @SerializedName("attractions")
    public List<List<Attraction>> attractions;
}
