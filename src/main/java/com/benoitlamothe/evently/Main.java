package com.benoitlamothe.evently;

import com.benoitlamothe.evently.handlers.TestHandler;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import static spark.Spark.*;

/**
 * Created by olivier on 2017-01-28.
 */
public class Main {
    public static void main(String[] args) {

        /*try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Missing MySQL Driver");
            e.printStackTrace();
            return;
        }*/

        final HikariConfig config = new HikariConfig("hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);
        final GsonBuilder builder = new GsonBuilder();

        get("/hello", new TestHandler(ds, builder.create()));
    }
}
