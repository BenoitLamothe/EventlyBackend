package com.benoitlamothe.evently.utils;

import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * Created by olivier on 2017-01-28.
 */
public class JsonTransformer implements ResponseTransformer {
    private Gson gson;

    public JsonTransformer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}
