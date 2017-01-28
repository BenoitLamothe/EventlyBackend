package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.utils.GeoUtil;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by olivier on 2017-01-28.
 */
public class ParksOpenDataMiner {
    private static final String PARKS_URL = "http://donnees.shawinigan.opendata.arcgis.com/datasets/084ddba0470f4c0fb3b5beff2a6f5467_0.geojson";

    private static final HikariConfig config = new HikariConfig("/hikari.properties");
    private static final HikariDataSource ds = new HikariDataSource(config);

    public static void main(String[] args) throws UnirestException, SQLException {
        JSONObject eventsObj = Unirest.get(PARKS_URL).asJson().getBody().getObject();
        JSONArray feature = eventsObj.getJSONArray("features");

        Connection dbConn = ds.getConnection();

        for(int i = 0; i < feature.length(); i++) {
            JSONObject featureObj = feature.getJSONObject(i);
            JSONObject featurePropertiesObj = featureObj.getJSONObject("properties");

            Attraction attrc = new Attraction();
            attrc.name = featurePropertiesObj.getString("NOM");
            attrc.location = featurePropertiesObj.getString("Adresse");


            String rtype = featurePropertiesObj.getString("TYPE");

            if(rtype.equals("Parc")) {
                attrc.categories = "Park";
            } else if (rtype.equals("Espace vert")) {
                attrc.categories = "Green Space";
            }

            if(!attrc.categories.equals("")) {
                attrc.getSQLInsert(dbConn).executeUpdate();
                System.out.println("Inserted attraction " + i);
            }
        }
    }
}
