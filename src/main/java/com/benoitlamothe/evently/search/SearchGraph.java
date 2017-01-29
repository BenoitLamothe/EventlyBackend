package com.benoitlamothe.evently.search;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.entity.criterias.ContentQualityCriteria;
import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.benoitlamothe.evently.utils.GeoUtil;
import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class SearchGraph {
    private static final int GRAPH_FAN = 10;
    private Event event;
    private List<ScheduleCriteria> enabledCriterias;
    private Map<Integer, Attraction> attractions;
    private DateTime lowerBound;
    private DateTime higherBound;
    private GraphNode generatedStartNode;
    private boolean randomize;

    public SearchGraph(Event event, Map<Integer, Attraction> attractions, List<ScheduleCriteria> criterias, boolean randomize, DateTime lowerBound, DateTime higherBound) throws SQLException {
        this.event = event;
        this.enabledCriterias = criterias;
        this.lowerBound = lowerBound;
        this.higherBound = higherBound;
        this.attractions = attractions;
        this.randomize = randomize;

        this.enabledCriterias.add(new ContentQualityCriteria());
    }

    public List<List<Attraction>> listPaths() {
        List<Attraction> sortedAttraction = attractions.values().stream().sorted((a, b) -> {
            Double positionFactor = GeoUtil.distance(this.event.latitude, a.latitude, this.event.longitude, a.longitude, 0, 0) - GeoUtil.distance(this.event.latitude, b.latitude, this.event.longitude, b.longitude, 0, 0);
            positionFactor += enabledCriterias.stream().map(x -> x.computeScrore(a, b)).reduce(0.0, (c, d) -> c + d);
            return positionFactor.intValue();
        }).collect(Collectors.toList());

        Attraction mainAttraction;

        if (this.randomize) {
            List<Attraction> shuffled = sortedAttraction.subList(0, 10);
            Collections.shuffle(shuffled);
            mainAttraction = shuffled.get(0);
        } else {
            mainAttraction = sortedAttraction.get(0);
        }

        List<Attraction> before = naive(mainAttraction, new ArrayList<>(), -1);
        sortedAttraction.removeAll(before);

        if (this.randomize) {
            List<Attraction> shuffled = sortedAttraction.subList(0, 10);
            Collections.shuffle(shuffled);
            mainAttraction = shuffled.get(0);
        } else {
            mainAttraction = sortedAttraction.get(0);
        }

        List<Attraction> after = naive(mainAttraction, before, 1);

        List<List<Attraction>> ret = new ArrayList<List<Attraction>>() {{
            add(before.subList(0, before.size() > 4 ? 4 : Math.max(before.size() - 1, 0)));
            add(after.subList(0, after.size() > 4 ? 4 : Math.max(after.size() - 1, 0)));
        }};

        return ret;
    }

    public Optional<Integer> getBestAttractionFromCriterias(Attraction from, List<Attraction> scheduleSoFar) {
        List<Integer> sortedAttraction = attractions.keySet().stream().sorted(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                Attraction o1A = attractions.get(o1);
                Attraction o2A = attractions.get(o2);
                List<Double> o1AVal = enabledCriterias.stream().map(x -> x.computeScrore(from, o1A)).collect(Collectors.toList());
                List<Double> o2AVal = enabledCriterias.stream().map(x -> x.computeScrore(from, o2A)).collect(Collectors.toList());
                Optional<Double> o1Sum = o1AVal.stream().reduce((x, y) -> x + y);
                Optional<Double> o2Sum = o2AVal.stream().reduce((x, y) -> x + y);
                Double o1SSum = o1Sum.isPresent() ? o1Sum.get() : 0.0;
                Double o2SSum = o2Sum.isPresent() ? o2Sum.get() : 0.0;

                return o1SSum.compareTo(o2SSum);
            }
        }).collect(Collectors.toList());

        List<Integer> attractionIdSoFar = scheduleSoFar.stream().map(x -> x.id).collect(Collectors.toList());
        sortedAttraction = sortedAttraction.stream().filter(x -> !attractionIdSoFar.contains(x)).collect(Collectors.toList());


        if (this.randomize) {
            List<Integer> shuffled = sortedAttraction.subList(0, sortedAttraction.size() / 2);
            Collections.shuffle(shuffled);
            return shuffled.stream().findFirst();
        } else {
            return sortedAttraction.stream().findFirst();
        }
    }

    public List<Attraction> naive(Attraction fromAttraction, List<Attraction> exclusions, int factor) {
        DateTime currentTime = factor > 0 ? this.event.endTime : this.event.startTime;
        DateTime limitTime = factor > 0 ? this.higherBound : this.lowerBound;
        boolean keepOnGoing = true;
        LinkedList<Attraction> currentAttractionSet = new LinkedList<>();
        currentAttractionSet.add(fromAttraction);

        while (keepOnGoing) {
            LinkedList<Attraction> temp = new LinkedList<>();
            temp.addAll(exclusions);
            temp.addAll(currentAttractionSet);
            temp.addAll(this.computeExclusions(currentAttractionSet));
            Optional<Integer> addAttractionOpt = this.getBestAttractionFromCriterias(fromAttraction, temp);
            if (!addAttractionOpt.isPresent()) {
                break;
            }
            Attraction addAttraction = attractions.get(addAttractionOpt.get());
            currentAttractionSet.add(addAttraction);

            keepOnGoing = factor < 0 ? currentTime.isAfter(limitTime) : currentTime.isBefore(limitTime);
            if (factor > 0) {
                currentTime = currentTime.plusMinutes(addAttraction.duration);
            } else {
                currentTime = currentTime.minusMinutes(addAttraction.duration);
            }
        }
        return currentAttractionSet;
    }

    public List<Attraction> computeExclusions(List<Attraction> current) {
        List<Attraction> exclusions = new LinkedList<>();

        if (current.stream().filter(Attraction::isRestaurant).count() >= 1) {
            exclusions.addAll(this.attractions.values().stream().filter(Attraction::isRestaurant).collect(Collectors.toList()));
        }

        if (current.stream().filter(Attraction::isHotel).count() >= 1) {
            exclusions.addAll(this.attractions.values().stream().filter(Attraction::isHotel).collect(Collectors.toList()));
        }

        if (current.stream().filter(Attraction::isPark).count() >= 2) {
            exclusions.addAll(this.attractions.values().stream().filter(Attraction::isPark).collect(Collectors.toList()));
        }

        if (current.stream().filter(Attraction::isHeritage).count() >= 2) {
            exclusions.addAll(this.attractions.values().stream().filter(Attraction::isHeritage).collect(Collectors.toList()));
        }

        return exclusions;
    }
}
