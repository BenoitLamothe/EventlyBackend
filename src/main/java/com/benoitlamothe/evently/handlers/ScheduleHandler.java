package com.benoitlamothe.evently.handlers;

import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;

/**
 * Created by olivier on 2017-01-28.
 */
public class ScheduleHandler extends BaseHandler {

    public ScheduleHandler(DataSource dataSource, Gson serializer) {
        super(dataSource, serializer);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        ScheduleRequest sr = this.serializer.fromJson(request.body(), ScheduleRequest.class);

        return null;
    }

    public static class ScheduleRequest {

        enum Availability {
            @SerializedName("morning")
            MORNING,
            @SerializedName("afternoon")
            AFTERNOON,
            @SerializedName("evening")
            EVENING
        }

        @SerializedName("eventId")
        public int eventId;

        @SerializedName("availability")
        public Availability[] availability;

        @SerializedName("criterias")
        public ScheduleCriteria[] criterias;
    }
}
