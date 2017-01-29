package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Asset;
import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.utils.CloudUtils;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class PatrimoineOpenDataMiner {
    private static final String EVENTS_URL = "http://donnees.shawinigan.opendata.arcgis.com/datasets/e554cc5c6da546bb8b1702cb97e5b58e_0.geojson";

    private static final HikariConfig config = new HikariConfig("/hikari.properties");
    private static final HikariDataSource ds = new HikariDataSource(config);
    private static Connection dbConn;

    static {
        try {
            dbConn = ds.getConnection();
            dbConn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mine() throws UnirestException, SQLException, IOException {
        JSONObject eventsObj = Unirest.get(EVENTS_URL).asJson().getBody().getObject();
        JSONArray feature = eventsObj.getJSONArray("features");

        for(int i = 0; i < feature.length(); i++) {
            JSONObject featureObj = feature.getJSONObject(i);
            JSONObject featurePropertiesObj = featureObj.getJSONObject("properties");

            Attraction attr = new Attraction();
            attr.name = featurePropertiesObj.getString("Nom_Patrim");
            if(attr.name.isEmpty()) { continue; }
            LinkedList<Double> longs = new LinkedList<Double>();
            LinkedList<Double> lats = new LinkedList<Double>();
            for(int j = 0; j < featureObj.getJSONObject("geometry").getJSONArray("coordinates").length(); j++) {
                for (int jj = 0; jj < featureObj.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(j).length(); jj++) {
                    if(featureObj.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(j).getJSONArray(jj).length() < 2) {
                        continue;
                    }

                    try {
                        double loc_long = featureObj.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(j).getJSONArray(jj).getDouble(0);
                        double loc_lat = featureObj.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(j).getJSONArray(jj).getDouble(1);
                        longs.add(loc_long);
                        lats.add(loc_lat);
                    } catch (Throwable e) {

                    }
                }
            }

            if(longs.size() == 0 || lats.size() == 0) {
                continue;
            }

            attr.longitude = (float) (longs.stream().reduce((x, y) -> x+y).get() / longs.size());
            attr.latitude = (float) (lats.stream().reduce((x, y) -> x+y).get() / lats.size());
            attr.location = featurePropertiesObj.getString("Nom_Patrim");
            attr.link = EVENTS_URL;
            attr.categories = "Heritage";

            PreparedStatement pstmt = attr.getSQLInsert(dbConn);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();

            rs.next();

            if(!featurePropertiesObj.getString("Lien_Photo").isEmpty()) {
                Asset asset = new Asset();
                asset.attactionId = rs.getInt(1);
                asset.type = "Image";
                asset.url = CloudUtils.STORAGE_URI + CloudUtils.downloadToBucket("http://" + featurePropertiesObj.getString("Lien_Photo")).getName();

                asset.getSQLInsert(dbConn).executeUpdate();
                System.out.println("Downloaded " + asset.url);
            }

            System.out.println("Inserted event " + i);
        }

        dbConn.commit();
    }

    public static void main(String[] args) throws UnirestException, SQLException, IOException {
        PatrimoineOpenDataMiner miner = new PatrimoineOpenDataMiner();
        miner.mine();
    }
}
