package com.legyver.gradle.builddata.transform.slel;

import com.legyver.core.exception.CoreException;
import com.legyver.gradle.builddata.transform.TransformationAPI;
import com.legyver.utils.propcross.PropertyGraph;
import com.legyver.utils.propcross.SlelOperationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SlelTransformationAPI implements TransformationAPI {
    private static final String SLEL_VARIABLE_SUFFIX = ".format";
    private final Map<String, Object> transformationMap;

    public SlelTransformationAPI(Map<String, Object> transformationMap) {
        this.transformationMap = transformationMap;
    }

    @Override
    public Properties transform(Properties input) throws CoreException {
        Map<String, Object> propertyMap = new HashMap<>();
        //construct the propertyMap with the transformationMap values with their keys being marked with suffix
        for (String key: transformationMap.keySet()) {
            Object value = transformationMap.get(key);
            propertyMap.put(key + SLEL_VARIABLE_SUFFIX, value);
        }
        //add in the values from the input file
        for (String key : input.stringPropertyNames()) {
            String value = input.getProperty(key);
            propertyMap.put(key, value);
        }
        PropertyGraph propertyGraph = new PropertyGraph(propertyMap);
        propertyGraph.runGraph(new SlelOperationContext(SLEL_VARIABLE_SUFFIX));

        Properties result = new Properties();
        for (String key : propertyMap.keySet()) {
            result.put(key, propertyMap.get(key));
        }

        return result;
    }

}
