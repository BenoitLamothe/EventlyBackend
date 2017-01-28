package com.benoitlamothe.evently.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by olivier on 2017-01-28.
 */
public class AttractionReview {

    @SerializedName("rating")
    public int rating;

    @SerializedName("review")
    public String review;

    @SerializedName("date")
    public Date date;
}
