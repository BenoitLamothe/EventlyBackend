package com.benoitlamothe.evently.handlers;

import com.benoitlamothe.evently.entity.Event;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;

/**
 * Created by olivier on 2017-01-28.
 */
public class EventsHandler extends BaseHandler {

    public EventsHandler(DataSource dataSource) {
        super(dataSource, null);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return Event.getAll(this.dataSource.getConnection());
    }
}
