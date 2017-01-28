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
        return null;
    }

    public static class ScheduleRequest {

        enum Availability {
            MORNING("morning"),
            AFTERNOON("afternoon"),
            EVENING("evening");

            private String value;

            Availability(String value) {
                this.value = value;
            }

            public String getValue() {
                return this.value;
            }
        }

        @SerializedName("eventId")
        public int eventId;

        @SerializedName("availability")
        public Availability[] availability;

        @SerializedName("criterias")
        public ScheduleCriteria[] criterias;
    }
}
