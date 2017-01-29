package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by jeremiep on 2017-01-29.
 */
public class RatingCriteria extends ScheduleCriteria {
    private double value;

    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                RatingCriteria rc = new RatingCriteria();
                rc.value = json.getAsJsonObject().get("value").getAsDouble();
                return rc;
            }
        };
    }

    @Override
    JsonObject serializer() {
        return null;
    }

    @Override
    public double computeScrore(Attraction from, Attraction to) {
        if(to.rating < this.value) {
            return this.value - to.rating * -10;
        }

        return to.rating - this.value * 10;
    }
}
