package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.search.GraphNode;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class BudgetCriteria extends ScheduleCriteria {
    private double value;

    @Override
    JsonDeserializer<? extends ScheduleCriteria> deserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                BudgetCriteria criteria = new BudgetCriteria();
                criteria.value = json.getAsJsonObject().get("value").getAsDouble();
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
    public double computeScrore(GraphNode from, GraphNode to) {
        return 0.0;
    }
}
