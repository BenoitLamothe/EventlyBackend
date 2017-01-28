package com.benoitlamothe.evently.handlers;

import com.benoitlamothe.evently.entity.Event;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Created by olivier on 2017-01-28.
 */
public class EventsHandler extends BaseHandler {

    public EventsHandler(DataSource dataSource) {
        super(dataSource, null);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Connection conn = this.dataSource.getConnection();
        List<Event> events = Event.getAll(conn);
        conn.close();

        return events;
    }
}
