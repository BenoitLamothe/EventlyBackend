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

    public List<GraphNode> generateSearchTree(GraphNode currentNode, Collection<Integer> toBeExplored, DateTime currentTime, DateTime timeLimit, int incrementFactor, List<GraphNode> acc) {
        if((incrementFactor > 0 && currentTime.isAfter(timeLimit)) || (incrementFactor < 0 && currentTime.isBefore(timeLimit))) {
            return acc;
        } else {
            for(Integer id : toBeExplored) {
                GraphNode node = new GraphNode();
                node.data = loadedAttractions.get(id);

                GraphEdge edge = new GraphEdge();
                edge.start = currentNode;
                edge.end = node;

                currentNode.connections.add(edge);
                node.connections.add(edge);

                currentTime.plusMinutes(loadedAttractions.get(id).duration * incrementFactor);
                List<Integer> toBeExploredCopy = toBeExplored.stream().filter(u -> u != id).collect(Collectors.toList());
                acc.add(node);

                List<GraphNode> subgraph = generateSearchTree(node,
                        toBeExploredCopy,
                        currentTime,
                        timeLimit,
                        incrementFactor,
                        acc);

                acc.addAll(subgraph);
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);

        SearchGraph g = new SearchGraph(ds.getConnection(), new LinkedList<ScheduleCriteria>());
        Event evt = new Event();
        evt.startTime = DateTime.now().toDate();

        GraphNode currentNode = new GraphNode();

        List<GraphNode> graph = g.generateSearchTree(currentNode,
                g.loadedAttractions.keySet(),
                DateTime.now(),
                DateTime.now().plusMinutes(-4 * 60),
                -1,
                new LinkedList<>());
        System.out.println("Generated!!");
    }
}
