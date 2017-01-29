package com.benoitlamothe.evently;

import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.benoitlamothe.evently.handlers.EventsHandler;
import com.benoitlamothe.evently.handlers.ScheduleHandler;
import com.benoitlamothe.evently.utils.JsonTransformer;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.TimeZone;

import static spark.Spark.*;

/**
 * Created by olivier on 2017-01-28.
 */
public class Main {

    public static Storage defaultCloudStorage = null;

    static {
        try {
            defaultCloudStorage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.getApplicationDefault())
                    .setProjectId("evently-157015")
                    .build()
                    .getService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York")));
    }

    public static void main(String[] args) {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        config.setConnectionTimeout(5000);
        config.setInitializationFailTimeout(5000);
        final HikariDataSource ds = new HikariDataSource(config);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ScheduleCriteria.class, ScheduleCriteria.getCriteriasDeserializer());
        builder.registerTypeAdapter(ScheduleCriteria.class, ScheduleCriteria.getCriteriasSerializer());
        builder.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
            @Override
            public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                // Do not try to deserialize null or empty values
                if (json.getAsString() == null || json.getAsString().isEmpty())
                {
                    return null;
                }

                final DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
                return fmt.parseDateTime(json.getAsString());
            }
        });
        builder.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
            @Override
            public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
                final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                return new JsonPrimitive(fmt.print(src));
            }

        });

        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        enableCORS("*", "GET POST PUT OPTIONS", "");

        get("/events", new EventsHandler(ds), new JsonTransformer(builder.create()));

        post("/schedule", new ScheduleHandler(ds, builder.create()), new JsonTransformer(builder.create()));

        exception(Exception.class, (exception, request, response) -> {
            // Handle the exception here
            exception.printStackTrace();
        });
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }
}
