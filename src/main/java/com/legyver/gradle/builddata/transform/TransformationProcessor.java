package com.legyver.gradle.builddata.transform;

import com.legyver.core.exception.CoreException;
import com.legyver.gradle.builddata.BuildMetaAlgorithm;
import com.legyver.utils.propl.PropertyList;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class TransformationProcessor {
    private static final Logger logger = Logging.getLogger(TransformationProcessor.class);

    private final TransformationAPI transformationAPI;
    private final BuildMetaAlgorithm buildMetaAlgorithm;
    private final Set<String> sendToTarget;
    private final Set<String> sendToSource;

    private String version;

    public TransformationProcessor(TransformationAPI transformationAPI, BuildMetaAlgorithm buildMetaAlgorithm, Set<String> sendToTarget, Set<String> sendToSource) {
        this.transformationAPI = transformationAPI;
        this.buildMetaAlgorithm = buildMetaAlgorithm;
        this.sendToTarget = sendToTarget;
        this.sendToSource = sendToSource;
        if (logger.isDebugEnabled()) {
            logger.debug("Configured: {}", this);
        }
    }

    public void transform(File sourceFile, File outputFile) {
        PropertyList input = new PropertyList();
        PropertyList target = new PropertyList();

       try (FileInputStream sourceInputStream = new FileInputStream(sourceFile)) {
            input.load(sourceInputStream);
            if (outputFile != null && outputFile.exists()) {
                //not using properties because we want to preserve order
                target.load(outputFile);
            }
       } catch (FileNotFoundException fileNotFoundException) {
            logger.error("File not found: " + sourceFile.getName(), fileNotFoundException);
            throw new RuntimeException(fileNotFoundException);
       } catch (IOException ioException) {
            logger.error("Error reading file: " + sourceFile.getName(), ioException);
            throw new RuntimeException(ioException);
       }

       try {
           //make a new copy, we don't want to write all our work do the source file at the end
           Properties working = new Properties();
           for (String key : input.stringPropertyNames()) {
               working.put(key, input.getProperty(key));
           }
           updateBuildMeta(working);

           Properties transformed = transformationAPI.transform(working);
           version = transformed.getProperty("version");
           logger.debug("Setting version: {}", version);

           updateTarget(outputFile, target, transformed);
           updateSource(sourceFile, input, transformed);
       } catch (CoreException exception) {
           logger.error("Error transforming values", exception);
           throw new RuntimeException(exception);
       } catch (IOException ioException) {
           logger.error("Saving values values", ioException);
           throw new RuntimeException(ioException);
       }
    }

    private void updateSource(File sourceFile, PropertyList input, Properties transformed) throws IOException {
        export(sourceFile, "source", input, transformed, sendToSource);
    }

    private void updateTarget(File outputFile, PropertyList target, Properties transformed) throws IOException {
        export(outputFile, "target", target, transformed, sendToTarget);
    }

    private void export(File file, String name, PropertyList original, Properties transformed, Set<String> sendProperties) throws IOException {
        if (file != null && !sendProperties.isEmpty()) {
            for (String key : sendProperties) {
                //override any existing properties with transformed values
                String value = transformed.getProperty(key);
                original.put(key, value);
                logger.debug("Setting on {}: [{}, {}]", name, key, value);
            }
            logger.debug("Updating {} file: {}", name, file.getAbsolutePath());
            original.write(file);
        }
    }

    private void updateBuildMeta(Properties input) {
        String oldBuildMeta = input.getProperty("version.buildmeta");
        String newBuildMeta = buildMetaAlgorithm.apply(oldBuildMeta, input);
        logger.info("Recalculating buildmeta.  Old value [{}], new value [{}]", oldBuildMeta, newBuildMeta);
        input.put("compute.buildmeta", newBuildMeta);
        input.remove("version.buildmeta");//remove original value, we want it to be overridable via mappings
        //the default value just reads the compute.buildmeta value
        //it's conceivable that the user would want to insert their own compute.buildmeta configuration so delay the resolution
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "TransformationProcessor{" +
                "transformationAPI=" + transformationAPI +
                ", buildMetaAlgorithm=" + buildMetaAlgorithm +
                ", sendToTarget=[" + sendToTarget.stream().collect(Collectors.joining(", ")) + ']' +
                ", sendToSource=[" + sendToSource.stream().collect(Collectors.joining(", ")) + ']' +
                '}';
    }


}
