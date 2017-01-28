package com.benoitlamothe.evently.entity;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * Created by olivier on 2017-01-28.
 */
public abstract class ScheduleCriteria {


    static JsonSerializer<ScheduleCriteria> getSerializer() {
        return null;
    }
    static JsonDeserializer<ScheduleCriteria> getDeserializer() {
        return null;
    }
}
