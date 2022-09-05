package com.legyver.gradle.builddata;

import com.legyver.gradle.builddata.transform.TransformationAPI;
import com.legyver.gradle.builddata.transform.slel.SlelTransformationAPI;

import java.util.Map;

public enum TransformAlgorithm {
    SLEL {
        @Override
        TransformationAPI getTransformationAPI(Map<String, Object> transformationMap) {
            return new SlelTransformationAPI(transformationMap);
        }
    };

    abstract TransformationAPI getTransformationAPI(Map<String, Object> transformationMap);

}
