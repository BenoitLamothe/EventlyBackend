package com.benoitlamothe.evently.search;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.sun.javafx.geom.Line2D;
import com.sun.tools.internal.xjc.reader.gbind.Graph;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
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

    public SearchGraph(Event event, Map<Integer, Attraction> attractions, List<ScheduleCriteria> criterias, DateTime lowerBound, DateTime higherBound) throws SQLException {
        this.event = event;
        this.enabledCriterias = criterias;
        this.lowerBound = lowerBound;
        this.higherBound = higherBound;
        this.attractions = attractions;
    }

    public void getSubtree(GraphNode currentNode, Collection<Integer> toBeExplored, DateTime currentTime, DateTime timeLimit, int incrementFactor) {
        if ((incrementFactor > 0 && currentTime.isAfter(timeLimit)) || (incrementFactor < 0 && currentTime.isBefore(timeLimit)) || currentTime.isEqual(timeLimit)) {
            GraphNode endNode = new GraphNode();
            endNode.currentType = GraphNode.EndpointType.END;
            currentNode.children.add(endNode);
        } else {
            System.out.println(currentTime.toString() + " limit: " + timeLimit.toString() + "incrment: " + incrementFactor);
            for (Integer id : toBeExplored) {
                GraphNode node = new GraphNode();
                node.data = attractions.get(id);
                currentNode.children.add(node);

                DateTime nextDate = incrementFactor > 0 ? currentTime.plusMinutes(attractions.get(id).duration) : currentTime.minusMinutes(attractions.get(id).duration);

                if (incrementFactor > 0 && nextDate.isBefore(currentTime)) {
                    throw new RuntimeException();
                } else if (incrementFactor < 0 && nextDate.isAfter(currentTime)) {
                    throw new RuntimeException();
                }

                List<Integer> toBeExploredCopy = toBeExplored.stream().filter(u -> !u.equals(id)).collect(Collectors.toList());
                toBeExploredCopy = this.getBestPromising(GRAPH_FAN, toBeExploredCopy, currentNode);
                if (toBeExploredCopy.size() != toBeExplored.size() - 1) {
//                    throw new RuntimeException();
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
        toBeExplored = this.getBestPromising(GRAPH_FAN, toBeExplored, new GraphNode());
        for (Integer id : toBeExplored) {
            GraphNode node = new GraphNode();
            node.data = attractions.get(id);
            nodes.add(node);

            DateTime nextDate = incrementFactor > 0 ? currentTime.plusMinutes(attractions.get(id).duration) : currentTime.minusMinutes(attractions.get(id).duration);
            List<Integer> toBeExploredCopy = toBeExplored.stream().filter(u -> !u.equals(id)).collect(Collectors.toList());
            toBeExploredCopy = this.getBestPromising(GRAPH_FAN, toBeExploredCopy, node);
            getSubtree(node,
                    toBeExploredCopy,
                    nextDate,
                    timeLimit,
                    incrementFactor);
        }
        return nodes;
    }

    private List<Integer> getBestPromising(int x, Collection<Integer> ints, GraphNode currentNode) {
        LinkedList<Integer> result = new LinkedList<>();
        PriorityQueue<Integer> toBeExploredSorted = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                GraphNode o1Node = new GraphNode();
                GraphNode o2Node = new GraphNode();

                o1Node.data = attractions.get(o1);
                o2Node.data = attractions.get(o2);
                return computeScore(currentNode, o1Node).compareTo(computeScore(currentNode, o2Node));
            }
        });
        ints.forEach(toBeExploredSorted::add);
        for (int i = 0; i < x; i++) {
            if (toBeExploredSorted.isEmpty()) {
                break;
            }

            result.add(toBeExploredSorted.remove());
        }

        return result;
    }

    public Double computeScore(GraphNode from, GraphNode to) {
        List<Double> mm = this.enabledCriterias.stream().map(x -> x.computeScrore(from, to)).collect(Collectors.toList());
        Optional<Double> sum = mm.stream().reduce((x, y) -> x + y);
        return sum.isPresent() ? sum.get() : 0.0;
    }

    public void generateGraph() {
        GraphNode startNode = new GraphNode();
        startNode.currentType = GraphNode.EndpointType.START;
        startNode.children.addAll(this.generateSearchTree(
                this.attractions.keySet(),
                DateTime.now(),
                DateTime.now().minusMinutes(4 * 60),
                -1));

        this.generatedStartNode = startNode;
    }

    public List<GraphNode> listPath(LinkedList<GraphNode> acc, GraphNode currentNode, LinkedList<GraphNode> excluded) {
        if(currentNode.currentType == GraphNode.EndpointType.END) {
            return acc;
        } else {
            acc.add(currentNode);
            List<GraphNode> children = currentNode.children;

            children.sort(new Comparator<GraphNode>() {
                @Override
                public int compare(GraphNode o1, GraphNode o2) {
                    return computeScore(currentNode, o1).compareTo(computeScore(currentNode, o2));
                }
            });
            Optional<GraphNode> optimalChild = children.stream().findFirst();
            if(!optimalChild.isPresent()) {
                throw new RuntimeException("Wtf");
            }

            return listPath(acc, optimalChild.get(), excluded);
        }
    }

    public Collection<List<GraphNode>> listPaths() {
        Collection<List<GraphNode>> paths = new ArrayList<>();
        LinkedList<GraphNode> excludedNodes = new LinkedList<>();
        for(GraphNode c : this.generatedStartNode.children) {
            List<GraphNode> nodes = this.listPath(new LinkedList<>(), c, excludedNodes);
            paths.add(nodes);
            excludedNodes.addAll(nodes);
        }

        return paths;
    }

    public static void main(String[] args) throws SQLException {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);

        Map<Integer, Attraction> attractions = Attraction.getAttractions(ds.getConnection())
                .stream()
                .collect(Collectors.toMap(Attraction::getID, Function.identity()));
        SearchGraph g = new SearchGraph(null, attractions, new LinkedList<ScheduleCriteria>(), DateTime.now(), DateTime.now().plusHours(3));
        g.generateGraph();
        Collection<List<GraphNode>> paths = g.listPaths();

        System.out.print("test");
    }
}
