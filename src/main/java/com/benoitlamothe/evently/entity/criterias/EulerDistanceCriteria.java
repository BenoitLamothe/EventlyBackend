package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
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

        return -1 * GeoUtil.distance(from.latitude, to.latitude,
                from.longitude, to.longitude, 0, 0);
    }
}
