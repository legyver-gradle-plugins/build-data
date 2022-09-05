package com.legyver.gradle.builddata;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Versioning schemes supported by this plugin.
 * These are expected provide the patterns supported by the versioning scheme
 */
public enum VersionScheme {
    /**
     * Semantic Versioning scheme allows updating versions in README's where the existing version in the README is a valid SemVer version,
     * as per known SemVer regular expression (currently just SemVer 2.0)
     */
    SEMVER(new HashMap<>() {
        {
            //this pattern comes from https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
            //only the beginning (^) and end ($) markers were removed
            this.put("2.0", "semver-2-0.properties");
        }
    });
    private final Map<String, String> versionedPatternFiles;

    VersionScheme(Map<String, String> versionedPatternFiles) {
        this.versionedPatternFiles = versionedPatternFiles;
    }

    public Properties getPatterns(String version) throws UnknownVersionException, IOException {
        Properties properties = new Properties();
        String fileName = versionedPatternFiles.get(version);
        if (fileName == null) {
            throw new UnknownVersionException(version);
        }
        try (InputStream inputStream = VersionScheme.class.getResourceAsStream(fileName)) {
            properties.load(inputStream);
        }
        return properties;
    }

    public String getSupportedVersions() {
        return versionedPatternFiles.keySet().stream().collect(Collectors.joining(", "));
    }
}
