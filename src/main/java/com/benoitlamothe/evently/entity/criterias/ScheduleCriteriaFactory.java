package com.benoitlamothe.evently.entity.criterias;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by olivier on 2017-01-28.
 */
public class ScheduleCriteriaFactory {
    static ScheduleCriteria build(String name) {
        if(name.equalsIgnoreCase("categories")) {
            return new CategoriesCriteria();
        }

        throw new NotImplementedException();
    }
}
