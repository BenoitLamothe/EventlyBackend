package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by olivier on 2017-01-29.
 */
public class TransportCriteria extends ScheduleCriteria {
    private String value;

    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                TransportCriteria criteria = new TransportCriteria();
                criteria.value = json.getAsJsonObject().get("value").getAsString();
                return criteria;
            }
        };
    }

    @Override
    JsonObject serializer() {
        JsonObject serialized = new JsonObject();
        serialized.add("name", new JsonPrimitive("categories"));
        serialized.add("value", new JsonPrimitive(this.value));
        return serialized;
    }

    @Override
    public double computeScrore(Attraction from, Attraction to) {
        return 0.0;
    }
}
