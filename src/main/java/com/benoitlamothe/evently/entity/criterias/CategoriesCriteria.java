package com.benoitlamothe.evently.entity.criterias;

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
    JsonDeserializer<? extends ScheduleCriteria> getDeserializer() {
        return new JsonDeserializer<CategoriesCriteria>() {
            @Override
            public CategoriesCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                CategoriesCriteria criteria = new CategoriesCriteria();
                criteria.categories = new LinkedList<String>();
                for(JsonElement e : json.getAsJsonObject().getAsJsonArray("values")) {
                    criteria.categories.add(e.getAsString());
                }

                return criteria;
            }
        };
    }
}
