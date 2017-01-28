package com.benoitlamothe.evently.handlers;

import spark.Route;

import javax.sql.DataSource;

/**
 * Created by olivier on 2017-01-28.
 */
abstract class BaseHandler implements Route {
    DataSource dataSource;

    BaseHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
