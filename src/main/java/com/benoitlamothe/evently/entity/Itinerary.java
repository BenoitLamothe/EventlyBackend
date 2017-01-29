package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;
import com.sun.tools.doclint.HtmlTag;

import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class Itinerary {
    @SerializedName("event")
    public Event event;

    @SerializedName("beforeEvents")
    public List<Attraction> beforeEvents;

    @SerializedName("afterEvents")
    public List<Attraction> afterEvent;
}
