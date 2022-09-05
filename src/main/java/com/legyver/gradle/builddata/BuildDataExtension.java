package com.legyver.gradle.builddata;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

/**
 * buildData {
 *     sourceFile = 'version.properties'
 *     targetFile = 'src/main/resources/build.properties'
 *     versionScheme = 'SEMVER' //default value if not specified
 *     versionSchemeVersion = '2.0' //default value if not specified
 *     transformAlgorithm = 'slel'  //default value if not specified
 *     buildMetaGenerationAlgorithm = 'increment'  //default value if not specified
 *     transformMap = [
 *             //below included for reference; these are the default mappings
 *             'major.version': '${version.major}',
 *             'minor.version': '${version.minor}',
 *             'patch.number': '${version.patch}',
 *             'pre-release': '${version.prerelease}',
 *             'build.number': '${compute.buildmeta}',
 *             //compute values calculated prior to graph evaluation
 *             'build.date.day' : '${compute.day}',
 *             'build.date.month' : '${compute.month-name}',//month-digit also available
 *             'build.date.year' : '${compute.year}',
 *             //semver2 patterns
 *             'build.version.pre-release': '${major.version}.${minor.version}.${patch.number}-${pre-release}',
 *             'build.version.pre-release.build': '${build.version.pre-release}+b${build.number}',
 *             'build.version.release': '${major.version}.${minor.version}.${patch.number}',
 *             'build.version.release.build': '${build.version.release}+b${build.number}',
 *
 *             'version' : '${build.version.pre-release}',//gradle artifact version omits build data
 *             'build.version' : '${build.version.pre-release.build}'//application build version includes build data
 *     ]
 *     sendToTarget = [
 *          'build.version'
 *     ]
 *  }
 */
public interface BuildDataExtension {
    Property<String> getVersionScheme();
    Property<String> getVersionSchemeVersion();
    Property<String> getSourceFile();
    Property<String> getTargetFile();
    Property<String> getTransformAlgorithm();
    Property<String> getBuildMetaGenerationAlgorithm();
    MapProperty<String, Object> getTransformMap();
    SetProperty<String> getSendToTarget();
    SetProperty<String> getSendToSource();
}
