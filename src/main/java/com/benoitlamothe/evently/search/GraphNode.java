package com.benoitlamothe.evently.search;

import com.benoitlamothe.evently.entity.Attraction;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class GraphNode {
    Attraction data;
    List<GraphNode> children = new LinkedList<>();
}
