package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Asset;
import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.utils.CloudUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by jeremiep on 2017-01-29.
 */
public class MauriceTourism {
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

    public void mine() throws UnirestException, SQLException, IOException {
        HttpResponse<String> htmlResp = Unirest.get("http://www.mauricietourism.com/member/chez-baza/").asString();
        String html = htmlResp.getBody();
        Document doc = Jsoup.parse(html);

        for(Element gdocElem : doc.getElementsByClass("google_map_info")) {
            Attraction attr = new Attraction();
            attr.name = gdocElem.getElementsByClass("title").first().text();
            attr.latitude = Float.parseFloat(gdocElem.getElementsByClass("lat").first().text());
            attr.longitude = Float.parseFloat(gdocElem.getElementsByClass("lng").first().text());
            attr.website = gdocElem.getElementsByClass("link").first().text();
            attr.link = "http://www.mauricietourism.com/";
            attr.location = gdocElem.getElementsByClass("addr").first().text() + ", " +
                    gdocElem.getElementsByClass("city").first().text() + ", " +
                    gdocElem.getElementsByClass("province").first().text() + ", " +
                    gdocElem.getElementsByClass("cp").first().text() + ", " +
                    gdocElem.getElementsByClass("country").first().text();
            String category = gdocElem.getElementsByClass("type_marker").first().text();
            switch (category) {
                case "activity":
                    attr.categories = "Sport";
                    break;

                case "loger":
                    attr.categories = "Hotel";
                    break;

                case "manger":
                    attr.categories = "Restaurants";
                    break;
            }

            PreparedStatement stmt = attr.getSQLInsert(dbConn);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();

            rs.next();
            if(gdocElem.getElementsByClass("img").first().getElementsByTag("img").first().hasAttr("data-lazy-src")) {
                String imgUrl = gdocElem.getElementsByClass("img").first().getElementsByTag("img").first().attr("data-lazy-src");
                Asset asset = new Asset();
                asset.type = "Image";
                asset.attactionId = rs.getInt(1);
                try {
                    asset.url = CloudUtils.STORAGE_URI + CloudUtils.downloadToBucket(imgUrl).getName();

                    asset.getSQLInsert(dbConn).executeUpdate();
                    System.out.println("downloaded " + asset.url);
                } catch (Throwable e) {

                }
            }

            System.out.println("Parsed " + attr.name);
        }
        dbConn.commit();
    }


    public static void main(String[] args) throws UnirestException, IOException, SQLException {
        MauriceTourism miner = new MauriceTourism();
        miner.mine();
    }
}
