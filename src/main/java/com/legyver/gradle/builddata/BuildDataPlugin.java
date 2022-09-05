package com.legyver.gradle.builddata;

import com.legyver.gradle.builddata.transform.TransformationAPI;
import com.legyver.gradle.builddata.transform.TransformationProcessor;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BuildDataPlugin implements Plugin<Settings> {
    private static final Logger logger = Logging.getLogger(BuildDataPlugin.class);

    @Override
    public void apply(Settings settings) {
        BuildDataExtension extension = settings.getExtensions()
                .create("buildData", BuildDataExtension.class);
        settings.getGradle().allprojects(project -> {
            String sourceFileName = extension.getSourceFile().get();
            File sourceFile = project.file(sourceFileName);
            logger.debug("SourceFile [{}] exists: {}", sourceFile.getAbsolutePath(), sourceFile.exists());
            if (!sourceFile.exists()) {
                return;
            }

            String targetFileName = extension.getTargetFile().getOrElse(null);
            File targetFile = project.file(targetFileName);
            if (targetFile == null) {
                logger.debug("No target file specified");
            } else {
                logger.debug("TargetFile [{}] exists: {}", targetFile.getAbsolutePath(), targetFile.exists());
            }

            String versionSchemeName = extension.getVersionScheme().getOrElse("SEMVER");
            String versionSchemeVersion = extension.getVersionSchemeVersion().getOrElse("2.0");
            logger.debug("Setting VersionScheme to {} version {}", versionSchemeName, versionSchemeVersion);
            VersionScheme versionScheme = VersionScheme.valueOf(versionSchemeName);

            String transformAlgorithmName = extension.getTransformAlgorithm().getOrElse("slel");
            logger.debug("Transformation algorithm: {}", transformAlgorithmName);
            TransformAlgorithm transformAlgorithm = TransformAlgorithm.valueOf(transformAlgorithmName.toUpperCase());

            String buildMetaAlgorithmName = extension.getBuildMetaGenerationAlgorithm().getOrElse("increment");
            logger.debug("buildmeta algorithm: {}", buildMetaAlgorithmName);
            BuildMetaAlgorithm buildMetaAlgorithm = BuildMetaAlgorithm.valueOf(buildMetaAlgorithmName.toUpperCase());

            Set<String> sendToTarget = extension.getSendToTarget().getOrElse(new HashSet<>());
            Set<String> sendToSource = extension.getSendToSource().getOrElse(new HashSet<>());

            Map<String, Object> defaultMappings = new HashMap<>();
            try {
                addDefaultTransformations(defaultMappings, versionScheme.getPatterns(versionSchemeVersion));
            } catch (IOException|UnknownVersionException e) {
                logger.error("Error reading version", e);
            }

            Map<String, Object> specifiedMappings = extension.getTransformMap().getOrNull();
            if (specifiedMappings != null) {
                for (String key : specifiedMappings.keySet()) {
                    defaultMappings.put(key, specifiedMappings.get(key));
                }
            }

            TransformationAPI transformationAPI = transformAlgorithm.getTransformationAPI(defaultMappings);
            TransformationProcessor transformationProcessor = new TransformationProcessor(transformationAPI, buildMetaAlgorithm, sendToTarget, sendToSource);
            transformationProcessor.transform(sourceFile, targetFile);
            project.setVersion(transformationProcessor.getVersion());
        });


    }

    private void addDefaultTransformations(Map<String, Object> defaultMappings, Properties versionSchemeProperties) {
        defaultMappings.put("major.version", "${version.major}");
        defaultMappings.put("minor.version", "${version.minor}");
        defaultMappings.put("patch.number", "${version.patch}");
        defaultMappings.put("pre-release", "${version.prerelease}");
        defaultMappings.put("build.number", "${version.buildmeta}");
        defaultMappings.put("version.buildmeta", "${compute.buildmeta}");//update the version.buildmeta to the computed pattern
        //below two patterns are defined in the versionScheme properties
        defaultMappings.put("version", "${build.version.release}");
        defaultMappings.put("build.version", "${build.version.pre-release.build}");
        for (String key : versionSchemeProperties.stringPropertyNames()) {
            defaultMappings.put(key, versionSchemeProperties.getProperty(key));
        }
    }
}
