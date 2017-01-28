package com.benoitlamothe.evently;

import com.benoitlamothe.evently.handlers.TestHandler;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;

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

        /*try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Missing MySQL Driver");
            e.printStackTrace();
            return;
        }*/

        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);
        final GsonBuilder builder = new GsonBuilder();

        get("/hello", new TestHandler(ds, builder.create()));
    }
}
