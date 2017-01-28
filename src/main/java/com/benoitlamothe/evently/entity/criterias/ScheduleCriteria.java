package com.benoitlamothe.evently.entity.criterias;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by olivier on 2017-01-28.
 */
public abstract class ScheduleCriteria {

    abstract JsonDeserializer<? extends ScheduleCriteria> getDeserializer();

    static JsonDeserializer<ScheduleCriteria> getCriteriasDeserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                ScheduleCriteria crit = ScheduleCriteriaFactory.build(json.getAsJsonObject().get("name").getAsString());
                return crit.getDeserializer().deserialize(json, typeOfT, context);
            }
        };
    }
}
