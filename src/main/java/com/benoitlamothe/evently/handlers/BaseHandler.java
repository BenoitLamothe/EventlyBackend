package com.benoitlamothe.evently.handlers;

import com.google.gson.Gson;
import spark.Route;

import javax.sql.DataSource;

/**
 * Created by olivier on 2017-01-28.
 */
abstract class BaseHandler implements Route {
    DataSource dataSource;
    Gson serializer;

    BaseHandler(DataSource dataSource, Gson serializer) {
        this.dataSource = dataSource;
        this.serializer = serializer;
    }
}
