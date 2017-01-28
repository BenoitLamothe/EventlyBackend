package com.benoitlamothe.evently.handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;

/**
 * Created by olivier on 2017-01-28.
 */
public class TestHandler extends BaseHandler {

    public TestHandler(DataSource dataSource, Gson serializer) {
        super(dataSource, serializer);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return null;
    }
}
