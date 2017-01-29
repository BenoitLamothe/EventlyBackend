package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Event;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class EventsOpenDataMiner {
    private static final String EVENTS_URL = "http://donnees.shawinigan.opendata.arcgis.com/datasets/27a6950c40aa4388a91473ce27f1d21c_0.geojson";

    private static final HikariConfig config = new HikariConfig("/hikari.properties");
    private static final HikariDataSource ds = new HikariDataSource(config);
    private static Connection dbConn;

    private static LinkedList<String> MONTHS = new LinkedList<>();
    static {
        MONTHS = new LinkedList<>();
        MONTHS.add("JANVIER");
        MONTHS.add("FÉVRIER");
        MONTHS.add("MARS");
        MONTHS.add("AVRIL");
        MONTHS.add("MAI");
        MONTHS.add("JUIN");
        MONTHS.add("JUILLET");
        MONTHS.add("AOÛT");
        MONTHS.add("SEPTEMBRE");
        MONTHS.add("OCTOBRE");
        MONTHS.add("NOVEMBRE");
        MONTHS.add("DÉCEMBRE");

        try {
            dbConn = ds.getConnection();
            dbConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Date parseDate(String month, int year) {


        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, MONTHS.indexOf(month));
        cal.set(Calendar.YEAR, year);
        return cal.getTime();
    }

    private void mine() throws UnirestException, SQLException {
        JSONObject eventsObj = Unirest.get(EVENTS_URL).asJson().getBody().getObject();
        JSONArray feature = eventsObj.getJSONArray("features");

        for(int i = 0; i < feature.length(); i++) {
            JSONObject featureObj = feature.getJSONObject(i);
            JSONObject featurePropertiesObj = featureObj.getJSONObject("properties");

            Event evt = new Event();
            evt.name = featurePropertiesObj.getString("NOM");
            evt.location = featurePropertiesObj.getString("LIEU");
            evt.longitude = (float) featureObj.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
            evt.latitude = (float) featureObj.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
            evt.startTime = new DateTime(this.parseDate(featurePropertiesObj.getString("MOIS"), featurePropertiesObj.getInt("Annee")));
            evt.description = "";
            evt.link = "";

            evt.getSQLInsert(dbConn).executeUpdate();
            System.out.println("Inserted event " + i);
        }

        dbConn.commit();
    }

    public static void main(String[] args) throws UnirestException, SQLException {
        EventsOpenDataMiner miner = new EventsOpenDataMiner();
        miner.mine();
    }
}
