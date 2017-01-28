package com.benoitlamothe.evently.handlers;

import com.google.gson.Gson;
import spark.Route;

import javax.sql.DataSource;

/**
 * Created by olivier on 2017-01-28.
 */
public abstract class BaseHandler implements Route {
    private DataSource dataSource;
    private Gson serializer;

    public BaseHandler(DataSource dataSource, Gson serializer) {
        this.dataSource = dataSource;
        this.serializer = serializer;
    }
}
