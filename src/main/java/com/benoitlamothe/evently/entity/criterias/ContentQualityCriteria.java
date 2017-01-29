package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by olivier on 2017-01-29.
 */
public class ContentQualityCriteria extends ScheduleCriteria {

    private static Map<String, Integer> qualities = new HashMap<String, Integer>(){{
        put("http://www.mauricietourism.com/", 100);
    }};

    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                ContentQualityCriteria criteria = new ContentQualityCriteria();
                return criteria;
            }
        };
    }

    @Override
    JsonObject serializer() {
        JsonObject serialized = new JsonObject();
        serialized.add("name", new JsonPrimitive("categories"));
        return serialized;
    }

    @Override
    public double computeScrore(Attraction from, Attraction to) {
        Integer fromQuality = 0;
        Integer toQuality = 0;
        for(Map.Entry<String, Integer> quality: qualities.entrySet()) {
            if(from.website.contains(quality.getKey())) {
                fromQuality = quality.getValue();
            }
            if(to.website.contains(quality.getKey())) {
                toQuality = quality.getValue();
            }
        }

        return fromQuality - toQuality;
    }
}
