package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
import com.benoitlamothe.evently.search.GraphNode;
import com.benoitlamothe.evently.utils.GeoUtil;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class EulerDistanceCriteria extends ScheduleCriteria {
    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return null;
    }

    @Override
    JsonObject serializer() {
        return null;
    }

    @Override
    public double computeScrore(Attraction from, Attraction to) {
        /*if(from.getType() != GraphNode.EndpointType.NORMAL || to.getType() != GraphNode.EndpointType.NORMAL) {
            return 0.0;
        }

        if(from.getAttraction() == null || to.getAttraction() == null) {
            return 0.0;
        }

        return -1 * GeoUtil.distance(from.getAttraction().latitude, to.getAttraction().latitude,
                                from.getAttraction().longitude, to.getAttraction().longitude, 0, 0);*/
        return 0.0;
    }
}
