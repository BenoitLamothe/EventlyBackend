package com.benoitlamothe.evently.entity;

import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class Attraction {

    public String name;

    public float latitude;

    public float longitude;

    public String location;

    public String hours;

    public int hoursShift;

    public String description;

    public String link;

    public String phone;

    public String website;

    public float reviewStars;

    public List<AttractionReview> reviews;

}
