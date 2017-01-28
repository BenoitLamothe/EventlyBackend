package com.benoitlamothe.evently;

import com.benoitlamothe.evently.handlers.EventsHandler;
import com.benoitlamothe.evently.handlers.ScheduleHandler;
import com.benoitlamothe.evently.utils.JsonTransformer;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ExceptionHandler;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

import static spark.Spark.*;

/**
 * Created by olivier on 2017-01-28.
 */
public class Main {

    public static Storage defaultCloudStorage = null;

    static {
        try {
            defaultCloudStorage = StorageOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("keys/Evently-66fe4241f3da.json")))
                    .setProjectId("evently-157015")
                    .build()
                    .getService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);
        GsonBuilder builder = new GsonBuilder();

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
