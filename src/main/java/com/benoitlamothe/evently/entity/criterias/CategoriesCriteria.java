package com.benoitlamothe.evently.entity.criterias;

import com.benoitlamothe.evently.search.GraphNode;
import com.google.common.collect.Lists;
import com.google.gson.*;
import jdk.nashorn.internal.runtime.options.Option;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if(!isValidNode(from) || !isValidNode(to)) {
            return 0.0;
        }

        String[] cat = to.getAttraction().categories.split(",");
        List<String> categories = Lists.newArrayList(cat);
        List<Integer> categoriesFound = categories.stream().map(x -> this.categories.contains(x) ? 1 : 0).collect(Collectors.toList());
        Optional<Integer> found = categoriesFound.stream().reduce((x, y) -> x + y);

        if(!found.isPresent()) {
            return 0.0;
        }

        return found.get() * 1000;
    }
}
