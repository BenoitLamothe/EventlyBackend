package com.benoitlamothe.evently.entity.criterias;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by jeremiep on 2017-01-28.
 */
public class BudgetCriteria extends ScheduleCriteria {
    private double value;

    @Override
    JsonDeserializer<? extends ScheduleCriteria> getDeserializer() {
        return new JsonDeserializer<ScheduleCriteria>() {
            @Override
            public ScheduleCriteria deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                BudgetCriteria criteria = new BudgetCriteria();
                criteria.value = json.getAsJsonObject().get("value").getAsDouble();
                return criteria;
            }
        };
    }
}
