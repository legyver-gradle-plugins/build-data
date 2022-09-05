package com.legyver.gradle.builddata.transform;

import com.legyver.core.exception.CoreException;

import java.util.Properties;

public interface TransformationAPI {
    Properties transform(Properties input) throws CoreException;
}
