package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Asset;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.utils.CloudUtils;
import com.benoitlamothe.evently.utils.GeoUtil;
import com.google.cloud.ExceptionHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by olivier on 2017-01-28.
 */
public class TourismMiner {

    public static void main(String[] args) throws IOException, SQLException {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);

        int paging = 0;
        ArrayList<Event> events = new ArrayList<>();
        while (true) {
            System.out.println(paging);
            Document doc = Jsoup.connect(String.format("http://www.tourismeshawinigan.com/evenements/%d", paging)).get();
            if (!shouldContinue(doc)) {
                break;
            }
            paging += 5;

            List<Event> cevents = getEvents(doc);
            events.addAll(cevents);
        }
        Connection conn = ds.getConnection();
        for (Event event : events) {
            PreparedStatement pstmt = event.getSQLInsert(conn);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                for (String url : event.imageSources) {
                    if (!url.startsWith("http")) {
                        continue;
                    }
                    Asset a = new Asset();
                    a.eventId = rs.getInt(1);
                    a.type = Asset.IMAGE_ASSET;
                    try {
                        a.url = CloudUtils.STORAGE_URI + CloudUtils.downloadToBucket(url).getName();
                        a.getSQLInsert(conn).execute();
                    } catch (Exception e) {
                    }
                }
            }

            System.out.println("Inserted: " + event.name);
        }
    }

    static boolean shouldContinue(Document doc) {
        return !doc.text().contains("Désolé, aucun événement disponible pour le moment...");
    }

    static List<Event> getEvents(Document doc) {
        Elements eventNodes = doc.select("article.event");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return eventNodes
                .stream()
                .map(x -> {
                    Event e = new Event();

                    e.name = sanitize(x.select("h1").get(0).text());

                    e.description = sanitize(x.childNodes()
                            .stream()
                            .filter(y -> {
                                if (y instanceof TextNode) {
                                    return true;
                                } else if (y instanceof Element) {
                                    Element p = (Element) y;
                                    return p.tagName().equals("p") && p.children().size() == 0;
                                } else {
                                    return false;
                                }
                            })
                            .map(y -> {
                                if (y instanceof TextNode) {
                                    return y.toString();
                                } else if (y instanceof Element) {
                                    return ((Element) y).text();
                                } else {
                                    return "";
                                }
                            })
                            .reduce("", (a, b) -> a + b));

                    try {
                        e.startTime = dateFormat.parse(x.select("time").get(0).attr("datetime"));
                        e.endTime = (Date) e.startTime.clone();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    Optional<Element> locationNode = x.select("strong")
                            .stream()
                            .filter(y -> y.text().equals("Lieux") || y.text().equals("Lieu"))
                            .findFirst();

                    if (locationNode.isPresent()) {
                        e.location = sanitize(locationNode.get().nextSibling().toString());
                        GeoUtil.LatLong latlng = GeoUtil.getLatLong(e.location);
                        e.latitude = latlng.latitude;
                        e.longitude = latlng.longitude;
                    } else {
                        e.location = "";
                    }

                    Optional<Element> linkNode = x.select("strong")
                            .stream()
                            .filter(y -> y.text().equals("Lien"))
                            .findFirst();

                    if (linkNode.isPresent() && linkNode.get().nextElementSibling() != null) {
                        e.link = linkNode.get().nextElementSibling().attr("href");
                    } else {
                        e.link = "";
                    }

                    Optional<Element> priceNode = x.select("strong")
                            .stream()
                            .filter(y -> y.text().equals("Coût"))
                            .findFirst();

                    if (priceNode.isPresent()) {
                        e.priceDisplay = sanitize(priceNode.get().nextSibling().toString());
                    } else {
                        e.priceDisplay = "";
                    }

                    //Heure

                    Optional<Element> hourNode = x.select("strong")
                            .stream()
                            .filter(y -> y.text().equals("Heure"))
                            .findFirst();

                    if (hourNode.isPresent()) {
                        String hourRaw = hourNode.get().nextSibling().toString();

                        Pattern rp = Pattern.compile("(?<fhh>[0-9]{1,2})h(?<fhm>[0-9]{1,2})?\\s*à\\s*(?<shh>[0-9]{1,2})h(?<shm>[0-9]{1,2})?");
                        Pattern p = Pattern.compile("(?<h>[0-9]{1,2})h(?<m>[0-9]{1,2})?");

                        Matcher rangeMatcher = rp.matcher(hourRaw);
                        Matcher fixedMatcher = p.matcher(hourRaw);

                        if (rangeMatcher.find()) {

                            DateTime dateFrom = new DateTime(e.startTime);

                            dateFrom = dateFrom.plusHours(Integer.parseInt(rangeMatcher.group("fhh")));


                            if(rangeMatcher.group("fhm") != null) {
                                dateFrom = dateFrom.plusMinutes(Integer.parseInt(rangeMatcher.group("fhm")));
                            }

                            boolean fromWasFuckedup = false;
                            if(dateFrom.getHourOfDay() < 6) {
                                fromWasFuckedup = true;
                                dateFrom = dateFrom.plusHours(12);
                            }

                            e.startTime = dateFrom.toDate();

                            DateTime dateTo = new DateTime(e.endTime);

                            dateTo = dateTo.plusHours(Integer.parseInt(rangeMatcher.group("shh")));

                            if(rangeMatcher.group("shm") != null) {
                                dateTo = dateTo.plusMinutes(Integer.parseInt(rangeMatcher.group("shm")));
                            }

                            if(fromWasFuckedup) {
                                dateTo = dateTo.plusHours(12);
                            }

                            e.startTime = dateTo.toDate();

                        } else if (fixedMatcher.find()) {

                            DateTime dateTime = new DateTime(e.startTime);

                            dateTime = dateTime.plusHours(Integer.parseInt(fixedMatcher.group("h")));

                            if (fixedMatcher.group("m") != null) {
                                dateTime = dateTime.plusMinutes(Integer.parseInt(fixedMatcher.group("m")));
                            }

                            if(dateTime.getHourOfDay() < 6) {
                                dateTime = dateTime.plusHours(12);
                            }

                            e.startTime = dateTime.toDate();
                            e.endTime = dateTime.plusHours(2).toDate();
                        } else {
                            DateTime dateTime = new DateTime(e.startTime);
                            if(dateTime.getHourOfDay() < 6) {
                                dateTime = dateTime.plusHours(12);
                            }

                            e.startTime = dateTime.toDate();
                            e.endTime = dateTime.plusHours(2).toDate();
                        }
                    }

                    e.imageSources = x.select("img")
                            .stream()
                            .map(y -> y.attr("src"))
                            .collect(Collectors.toList());

                    return e;
                })
                .collect(Collectors.toList());
    }

    static String sanitize(String val) {
        val = val.replaceAll("\\s*:\\s*", "");

        Document doc = new Cleaner(Whitelist.simpleText()).clean(Jsoup.parse(val));

        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        val = doc.body().html();

        val = val.replaceAll("&#xa0;", " ").trim();

        return val;
    }
}
