package com.benoitlamothe.evently.search;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.entity.criterias.CategoriesCriteria;
import com.benoitlamothe.evently.entity.criterias.EulerDistanceCriteria;
import com.benoitlamothe.evently.entity.criterias.ScheduleCriteria;
import com.sun.javafx.geom.Line2D;
import com.sun.tools.doclint.HtmlTag;
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

    /*public void getSubtree(GraphNode currentNode, Collection<Integer> toBeExplored, DateTime currentTime, DateTime timeLimit, int incrementFactor, int level) {
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
                toBeExploredCopy = this.getBestPromising(GRAPH_FAN * (level <= 0 ? 1 : level), toBeExploredCopy, currentNode);

                getSubtree(node,
                        toBeExploredCopy,
                        nextDate,
                        timeLimit,
                        incrementFactor,
                        level--);
            }
        }
    }

    public List<GraphNode> generateSearchTree(Collection<Integer> toBeExplored, DateTime currentTime, DateTime timeLimit, int incrementFactor, int estLevel, GraphNode start) {
        List<GraphNode> nodes = new LinkedList<>();
        toBeExplored = this.getBestPromising(GRAPH_FAN * (estLevel <= 0 ? 1 : estLevel), toBeExplored, start);
        for (Integer id : toBeExplored) {
            GraphNode node = new GraphNode();
            node.data = attractions.get(id);
            nodes.add(node);

            DateTime nextDate = incrementFactor > 0 ? currentTime.plusMinutes(attractions.get(id).duration) : currentTime.minusMinutes(attractions.get(id).duration);
            List<Integer> toBeExploredCopy = toBeExplored.stream().filter(u -> !u.equals(id)).collect(Collectors.toList());
            toBeExploredCopy = this.getBestPromising(GRAPH_FAN * (estLevel <= 0 ? 1 : estLevel), toBeExploredCopy, node);
            getSubtree(node,
                    toBeExploredCopy,
                    nextDate,
                    timeLimit,
                    incrementFactor,
                    estLevel--);
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
        startNode.data = this.event;
        startNode.currentType = GraphNode.EndpointType.START;
        startNode.children.addAll(this.generateSearchTree(
                this.attractions.keySet(),
                DateTime.now(),
                DateTime.now().minusMinutes(4 * 60),
                -1,
                4,
                startNode));

        this.generatedStartNode = startNode;
    }

    public List<GraphNode> listPath(LinkedList<GraphNode> acc, GraphNode currentNode, LinkedList<Integer> excluded) {
        if(currentNode.currentType == GraphNode.EndpointType.END) {
            return acc;
        } else {
            acc.add(currentNode);
            List<GraphNode> children = currentNode.children;

            children.sort(new Comparator<GraphNode>() {
                @Override
                public int compare(GraphNode o1, GraphNode o2) {
                    if(excluded.contains(o1.data.getId())) {
                        return -1;
                    } else if(excluded.contains(o2.data.getId())) {
                        return 1;
                    } else {
                        return computeScore(currentNode, o1).compareTo(computeScore(currentNode, o2));
                    }
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
        LinkedList<Integer> excludedNodes = new LinkedList<>();
        for(GraphNode c : this.generatedStartNode.children) {
            List<GraphNode> nodes = this.listPath(new LinkedList<>(), c, excludedNodes);
            paths.add(nodes);
            excludedNodes.addAll(nodes.stream().map(x -> x.data.getId()).collect(Collectors.toList()));
        }

        return paths;
    }
*/
    public Optional<Integer> getBestAttractionFromCriterias(Attraction from, LinkedList<Attraction> scheduleSoFar) {
        Collection<Integer> sortedAttraction = attractions.keySet().stream().sorted(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                Attraction o1A = attractions.get(o1);
                Attraction o2A = attractions.get(o2);
                List<Double> o1AVal = enabledCriterias.stream().map(x -> x.computeScrore(from, o1A)).collect(Collectors.toList());
                List<Double> o2AVal = enabledCriterias.stream().map(x -> x.computeScrore(from, o2A)).collect(Collectors.toList());
                Optional<Double> o1Sum = o1AVal.stream().reduce((x, y)-> x + y);
                Optional<Double> o2Sum = o2AVal.stream().reduce((x, y)-> x + y);
                Double o1SSum = o1Sum.isPresent() ? o1Sum.get() : 0.0;
                Double o2SSum = o2Sum.isPresent() ? o2Sum.get() : 0.0;

                return o1SSum.compareTo(o2SSum);
            }
        }).collect(Collectors.toList());

        List<Integer> attractionIdSoFar = scheduleSoFar.stream().map(x -> x.id).collect(Collectors.toList());
        sortedAttraction = sortedAttraction.stream().filter(x -> !attractionIdSoFar.contains(x)).collect(Collectors.toList());
        Optional<Integer> bestAttrId = sortedAttraction.stream().findFirst();
        return bestAttrId;
    }

    public void naive() {
        DateTime currentTime = DateTime.now();
        DateTime limitTime = currentTime.minusHours(4);
        int timeIncrementFactor = -1;
        boolean keepOnGoing = true;
        Attraction fromAttraction = null;
        LinkedList<Attraction> currentAttractionSet = new LinkedList<>();
        while(keepOnGoing) {
            Optional<Integer> addAttractionOpt = this.getBestAttractionFromCriterias(fromAttraction, currentAttractionSet);
            if(!addAttractionOpt.isPresent()) { break; }
            Attraction addAttraction = attractions.get(addAttractionOpt.get());
            currentAttractionSet.add(addAttraction);

            keepOnGoing = timeIncrementFactor < 0 ? currentTime.isAfter(limitTime) : currentTime.isBefore(limitTime);
            if(timeIncrementFactor > 0) {
                currentTime = currentTime.plusMinutes(addAttraction.duration);
            } else {
                currentTime = currentTime.minusMinutes(addAttraction.duration);
            }
        }

        System.out.print(currentAttractionSet.size());
    }

    public static void main(String[] args) throws SQLException {
        final HikariConfig config = new HikariConfig("/hikari.properties");
        final HikariDataSource ds = new HikariDataSource(config);

        Map<Integer, Attraction> attractions = Attraction.getAttractions(ds.getConnection())
                .stream()
                .collect(Collectors.toMap(Attraction::getID, Function.identity()));
        SearchGraph g = new SearchGraph(null, attractions, new LinkedList<ScheduleCriteria>(), DateTime.now(), DateTime.now().plusHours(3));
        CategoriesCriteria crit = new CategoriesCriteria();
        crit.categories.add("Sport");

        g.enabledCriterias.add(new EulerDistanceCriteria());
        g.enabledCriterias.add(crit);

        g.naive();
        System.out.print("test");
    }
}
