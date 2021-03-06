package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.entity.Attraction;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class CategoriesCriteria extends ScheduleCriteria {
    public List<String> categories = new LinkedList<>();

    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return new JsonDeserializer<CategoriesCriteria>() {
            @Override
            public CategoriesCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                CategoriesCriteria criteria = new CategoriesCriteria();
                criteria.categories = new LinkedList<String>();
                for (JsonElement e : json.getAsJsonObject().getAsJsonArray("value")) {
                    criteria.categories.add(e.getAsString());
                }

                return criteria;
            }
        };
    }

    @Override
    JsonObject serializer() {
        JsonObject serialized = new JsonObject();
        serialized.add("name", new JsonPrimitive("categories"));
        JsonArray arrValue = new JsonArray();
        categories
                .stream()
                .map(String::toLowerCase)
                .forEach(arrValue::add);
        serialized.add("value", arrValue);

        return serialized;
    }

    @Override
    public double computeScrore(Attraction from, Attraction to) {
        int fromScore = Stream.of(from.categories.split(","))
                .map(String::toLowerCase)
                .map(String::trim)
                .map(x -> this.categories.contains(x.toLowerCase()) ? 1 : 0)
                .reduce(0, (x, y) -> x + y);

        int toScore = Stream.of(to.categories.split(","))
                .map(String::toLowerCase)
                .map(String::trim)
                .map(x -> this.categories.contains(x.toLowerCase()) ? 1 : 0)
                .reduce(0, (x, y) -> x + y);

        return fromScore - toScore;
    }
}
