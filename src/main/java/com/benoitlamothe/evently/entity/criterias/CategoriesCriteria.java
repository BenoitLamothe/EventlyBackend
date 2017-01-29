package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.search.GraphNode;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class CategoriesCriteria extends ScheduleCriteria {
    private List<String> categories;

    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return new JsonDeserializer<CategoriesCriteria>() {
            @Override
            public CategoriesCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                CategoriesCriteria criteria = new CategoriesCriteria();
                criteria.categories = new LinkedList<String>();
                for(JsonElement e : json.getAsJsonObject().getAsJsonArray("value")) {
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
        categories.forEach(arrValue::add);
        serialized.add("value", arrValue);

        return serialized;
    }

    @Override
    public double computeScrore(GraphNode from, GraphNode to) {
        return 0.0;
    }
}
