package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class Itinerary {
    @SerializedName("event")
    public Event event;

    @SerializedName("beforeAttractions")
    public List<Attraction> beforeEvents;

    @SerializedName("afterAttractions")
    public List<Attraction> afterEvent;
}
