package com.benoitlamothe.evently.miners;

import com.benoitlamothe.evently.entity.Event;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
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

    public static GeoApiContext geoContext = new GeoApiContext().setApiKey("AIzaSyAYDNsIg7jX8OJYmhd81zpeHmG4aObW_2M");

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
            cevents.stream().forEach(x -> System.out.println(x.name));
            events.addAll(cevents);
        }
        Connection conn = ds.getConnection();
        for(Event event : events) {
            event.getSQLInsert(conn).execute();
        }
    }

    static boolean shouldContinue(Document doc) {
        return doc.text().indexOf("Désolé, aucun événement disponible pour le moment...") == -1;
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
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    Optional<Element> locationNode = x.select("strong")
                            .stream()
                            .filter(y -> y.text().equals("Lieux") || y.text().equals("Lieu"))
                            .findFirst();

                    if (locationNode.isPresent()) {
                        e.location = sanitize(locationNode.get().nextSibling().toString());
                        //LatLong latlng = getLatLong(e.location);
                        //e.latitude = latlng.latitude;
                        //e.longitude = latlng.longitude;
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

                        Pattern p = Pattern.compile("([0-9]{1,2})h([0-9]{1,2})?");

                        Matcher rangeMatcher = p.matcher(hourRaw);

                        if(rangeMatcher.matches()) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(e.startTime);

                            c.add(Calendar.HOUR, Integer.parseInt(rangeMatcher.group(1)));

                            if(rangeMatcher.groupCount() == 3) {
                                c.add(Calendar.MINUTE, Integer.parseInt(rangeMatcher.group(2)));
                            }
                        }
                    }

                    e.latitude = 0;
                    e.longitude = 0;

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

    static LatLong getLatLong(String query) {

        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoContext, query).await();
            GeocodingResult first = results[0];

            return new LatLong((float)first.geometry.location.lat, (float)first.geometry.location.lng);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class LatLong {
        public float latitude;
        public float longitude;

        public LatLong(float latitude, float longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
