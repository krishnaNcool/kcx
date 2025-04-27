package com.kcxutilities.core.dao;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class DefaultCronJobNotificationDao {

    @Resource(name = "flexibleSearchService")
    private FlexibleSearchService flexibleSearchService;

    public List<List<Object>> getQueryResultList(String customQuery, Collection<String> parameters, Collection<String> fieldClassTypeNames) {
        FlexibleSearchQuery query = new FlexibleSearchQuery(customQuery);

        // Add parameters
        Map<String, Object> queryParams = new HashMap<>();
        int index = 0;
        for (String param : parameters) {
            queryParams.put("param" + index++, param);
        }
        query.addQueryParameters(queryParams);

        // Convert and set as List<Class<?>>
        query.setResultClassList(new ArrayList<>(convertToClassList(fieldClassTypeNames)));

        // Search
        SearchResult<List<Object>> result = flexibleSearchService.search(query);

        return result.getResult();
    }

    private List<Class<?>> convertToClassList(Collection<String> classNames) {
        List<Class<?>> classes = new ArrayList<>();
        for (String className : classNames) {
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Invalid class name: " + className, e);
            }
        }
        return classes;
    }
}
