package com.benoitlamothe.evently.search;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.entity.Event;
import com.benoitlamothe.evently.entity.ILocalizable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class GraphNode {
    ILocalizable data;
    List<GraphNode> children = new LinkedList<>();
    EndpointType currentType = EndpointType.NORMAL;

    public enum EndpointType {
        START, END, NORMAL
    }

    public EndpointType getType() {
        return this.currentType;
    }

    public Attraction getAttraction() {
        return (Attraction) this.data;
    }
    public Event getEvent() { return (Event) this.data; }
}
