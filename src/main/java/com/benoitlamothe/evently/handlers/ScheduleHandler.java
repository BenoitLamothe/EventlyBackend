package com.benoitlamothe.evently.handlers;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.entity.Itinerary;
import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.benoitlamothe.evently.exception.EntityNotFoundException;
import com.benoitlamothe.evently.search.SearchGraph;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Connection conn = this.dataSource.getConnection();

        Event event = Event.byID(conn, sr.eventId);

        if(event == null) {
            return new EntityNotFoundException();
        }

        Map<Integer, Attraction> attractions = Attraction.getAttractions(conn)
                .stream()
                .collect(Collectors.toMap(Attraction::getID, Function.identity()));

        conn.close();

        DateTime lowerBound = sr.getAvailabilityLowerBound(event);
        DateTime higherBound = sr.getAvailabilityHigherBound(event);

        SearchGraph graph = new SearchGraph(event, attractions, sr.criterias, lowerBound, higherBound);

        List<List<Attraction>> paths = graph.listPaths();
        Itinerary itinerary = new Itinerary();
        itinerary.event = event;
        itinerary.beforeEvents = paths.get(0);
        itinerary.afterEvent = paths.get(1);

        return itinerary;
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
        public List<Availability> availability;

        @SerializedName("criterias")
        public List<ScheduleCriteria> criterias;

        public DateTime getAvailabilityLowerBound(Event event) {
            DateTime eventStart = new DateTime(event.startTime);

            if (eventStart.getHourOfDay() < 12) {
                //event start in am so use event start
                return eventStart;
            } else if (eventStart.getHourOfDay() < 18) {
                if (this.availability.contains(Availability.MORNING)) {
                    return eventStart.withHourOfDay(8);
                }
            } else {
                //event is night only
                if (this.availability.contains(Availability.MORNING)) {
                    return eventStart.withHourOfDay(8);
                } else if (this.availability.contains(Availability.AFTERNOON)) {
                    return eventStart.withHourOfDay(12);
                }
            }

            return eventStart;
        }

        public DateTime getAvailabilityHigherBound(Event event) {
            DateTime eventEnd = new DateTime(event.endTime);
            DateTime eventStart = new DateTime(event.startTime);

            if (eventEnd.getHourOfDay() > 18) {
                if (eventStart.getHourOfDay() > 18) {
                    //Night only
                    return eventStart;
                } else if (eventStart.getHourOfDay() > 12) {
                    //Pm and night
                    if (this.availability.contains(Availability.MORNING)) {
                        return eventStart.withHourOfDay(8);
                    }
                }
                return eventEnd;
            } else if (eventEnd.getHourOfDay() > 12) {
                if (this.availability.contains(Availability.EVENING)) {
                    return eventEnd.withHourOfDay(23);
                }

            } else {
                //event is am only
                if (this.availability.contains(Availability.EVENING)) {
                    return eventEnd.withHourOfDay(23);
                } else if (this.availability.contains(Availability.AFTERNOON)) {
                    return eventEnd.withHourOfDay(18);
                }
            }

            return eventStart;
        }
    }
}
