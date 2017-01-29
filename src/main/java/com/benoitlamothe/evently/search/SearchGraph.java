package com.benoitlamothe.evently.search;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.joda.time.DateTime;

import javax.smartcardio.ATR;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class SearchGraph {
    private Connection dbConn;
    private List<ScheduleCriteria> enabledCriterias;
    private HashMap<Integer, Attraction> loadedAttractions = new HashMap<Integer, Attraction>();

    public SearchGraph(Connection db, List<ScheduleCriteria> criterias) throws SQLException {
        this.dbConn = db;
        this.enabledCriterias = criterias;
        List<Attraction> attractions = Attraction.getAttractions(this.dbConn);
        attractions.forEach(x -> loadedAttractions.put(x.id, x));
    }

    public void generateFromEvent(Event evt, DateTime limit, int incrementFactor) throws SQLException {
        GraphNode startNode = new GraphNode();

    }

    public void getSubtree(GraphNode currentNode, Collection<Integer> toBeExplored, DateTime currentTime, DateTime timeLimit, int incrementFactor) {
        if ((incrementFactor > 0 && currentTime.isAfter(timeLimit)) || (incrementFactor < 0 && currentTime.isBefore(timeLimit)) || currentTime.isEqual(timeLimit)) {
            return;
        } else {
            System.out.println(currentTime.toString() + " limit: " + timeLimit.toString() + "incrment: " + incrementFactor);
            for (Integer id : toBeExplored) {
                GraphNode node = new GraphNode();
                node.data = loadedAttractions.get(id);
                currentNode.children.add(node);

                DateTime nextDate = incrementFactor > 0 ? currentTime.plusMinutes(loadedAttractions.get(id).duration) : currentTime.minusMinutes(loadedAttractions.get(id).duration);

                if(incrementFactor > 0 && nextDate.isBefore(currentTime)) {
                    throw new RuntimeException();
                } else if (incrementFactor < 0 && nextDate.isAfter(currentTime)) {
                    throw new RuntimeException();
                }

                List<Integer> toBeExploredCopy = toBeExplored.stream().filter(u -> !u.equals(id)).collect(Collectors.toList());
                if(toBeExploredCopy.size() != toBeExplored.size()-1) {
                    throw new RuntimeException();
                }

                getSubtree(node,
                        toBeExploredCopy,
                        nextDate,
                        timeLimit,
                        incrementFactor);
            }
        }
    }

    public List<GraphNode> generateSearchTree(Collection<Integer> toBeExplored, DateTime currentTime, DateTime timeLimit, int incrementFactor) {
        List<GraphNode> nodes = new LinkedList<>();
        for (Integer id : toBeExplored) {
            GraphNode node = new GraphNode();
            node.data = loadedAttractions.get(id);
            nodes.add(node);

            DateTime nextDate = incrementFactor > 0 ? currentTime.plusMinutes(loadedAttractions.get(id).duration) : currentTime.minusMinutes(loadedAttractions.get(id).duration);
            List<Integer> toBeExploredCopy = toBeExplored.stream().filter(u -> !u.equals(id)).collect(Collectors.toList());

            getSubtree(node,
                    toBeExploredCopy,
                    nextDate,
                    timeLimit,
                    incrementFactor);
        }
        return nodes;
    }

    public static void main(String[] args) throws SQLException {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);

        SearchGraph g = new SearchGraph(ds.getConnection(), new LinkedList<ScheduleCriteria>());
        Event evt = new Event();
        evt.startTime = DateTime.now().toDate();

        GraphNode currentNode = new GraphNode();

        List<GraphNode> graph = g.generateSearchTree(
                g.loadedAttractions.keySet(),
                DateTime.now(),
                DateTime.now().plusMinutes(-4 * 60),
                -1);
        System.out.println("Generated!!");
    }
}
