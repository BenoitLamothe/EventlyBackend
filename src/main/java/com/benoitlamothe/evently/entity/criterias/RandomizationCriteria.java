package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import java.util.Random;

/**
 * Created by jeremiep on 2017-01-29.
 */
public class RandomizationCriteria extends ScheduleCriteria {
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
        return new Random().nextInt(400);
    }
}
