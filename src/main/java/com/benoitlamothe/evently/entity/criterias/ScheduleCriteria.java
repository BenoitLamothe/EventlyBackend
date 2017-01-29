package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.search.GraphNode;
import com.google.api.client.json.Json;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by olivier on 2017-01-28.
 */
public abstract class ScheduleCriteria {

    abstract JsonDeserializer<? extends ScheduleCriteria> deserializer();
    abstract JsonObject serializer();
    public abstract double computeScrore(GraphNode from, GraphNode to);

    public static JsonSerializer<ScheduleCriteria> getCriteriasSerializer() {
        return new JsonSerializer<ScheduleCriteria>() {
            @Override
            public JsonElement serialize(ScheduleCriteria src, Type typeOfSrc, JsonSerializationContext context) {
                return src.serializer();
            }
        };
    }

    protected boolean isValidNode(GraphNode node) {
        return node.getAttraction() != null;
    }

    public static JsonDeserializer<ScheduleCriteria> getCriteriasDeserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                ScheduleCriteria crit = ScheduleCriteriaFactory.build(json.getAsJsonObject().get("name").getAsString());
                return crit.deserializer().deserialize(json, typeOfT, context);
            }
        };
    }
}
