/*
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.detect.bomtool

import java.nio.charset.StandardCharsets

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.detect.hub.HubSignatureScanner
import com.blackducksoftware.integration.hub.detect.model.BomToolType
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation
import com.blackducksoftware.integration.hub.detect.type.ExecutableType
import com.blackducksoftware.integration.hub.detect.util.executable.Executable
import com.google.gson.Gson

import groovy.transform.TypeChecked

@Component
@TypeChecked
class GradleBomTool extends BomTool {
    private final Logger logger = LoggerFactory.getLogger(GradleBomTool.class)

    static final String BUILD_GRADLE_FILENAME = 'build.gradle'

    @Autowired
    Gson gson

    @Autowired
    HubSignatureScanner hubSignatureScanner

    private String gradleExecutable

    BomToolType getBomToolType() {
        return BomToolType.GRADLE
    }

    boolean isBomToolApplicable() {
        def buildGradle = detectFileManager.findFile(sourcePath, BUILD_GRADLE_FILENAME)

        if (buildGradle) {
            gradleExecutable = findGradleExecutable(sourcePath)
            if (!gradleExecutable) {
                logger.warn('Could not find a Gradle wrapper or executable')
            }
        }

        buildGradle && gradleExecutable
    }

    List<DetectCodeLocation> extractDetectCodeLocations() {
        List<DetectCodeLocation> codeLocations = extractCodeLocationsFromGradle()

        File[] additionalTargets = detectFileManager.findFilesToDepth(detectConfiguration.sourceDirectory, 'build', detectConfiguration.searchDepth)
        if (additionalTargets) {
            additionalTargets.each { File file -> hubSignatureScanner.registerPathToScan(file) }
        }
        codeLocations
    }

    private String findGradleExecutable(String sourcePath) {
        String gradlePath = findExecutablePath(ExecutableType.GRADLEW, false, detectConfiguration.getGradlePath())

        if (!gradlePath) {
            logger.debug('gradle wrapper not found - trying to find gradle on the PATH')
            gradlePath = executableManager.getExecutablePath(ExecutableType.GRADLE, true, sourcePath)
        }
        gradlePath
    }

    List<DetectCodeLocation> extractCodeLocationsFromGradle() {
        File initScriptFile = detectFileManager.createFile(BomToolType.GRADLE, 'init-detect.gradle')
        String initScriptContents = getClass().getResourceAsStream('/init-script-gradle').getText(StandardCharsets.UTF_8.toString())
        initScriptContents = initScriptContents.replace('GRADLE_INSPECTOR_VERSION', detectConfiguration.getGradleInspectorVersion())
        initScriptContents = initScriptContents.replace('EXCLUDED_PROJECT_NAMES', detectConfiguration.getGradleExcludedProjectNames())
        initScriptContents = initScriptContents.replace('INCLUDED_PROJECT_NAMES', detectConfiguration.getGradleIncludedProjectNames())
        initScriptContents = initScriptContents.replace('EXCLUDED_CONFIGURATION_NAMES', detectConfiguration.getGradleExcludedConfigurationNames())
        initScriptContents = initScriptContents.replace('INCLUDED_CONFIGURATION_NAMES', detectConfiguration.getGradleIncludedConfigurationNames())

        String airgapLibsDirectoryPath = ''
        if (detectConfiguration.getGradleInspectorAirGapPath()) {
            airgapLibsDirectoryPath = new File(detectConfiguration.getGradleInspectorAirGapPath()).getCanonicalPath()
        }
        initScriptContents = initScriptContents.replace('AIRGAP_LIBS_DIRECTORY_PATH', airgapLibsDirectoryPath)

        detectFileManager.writeToFile(initScriptFile, initScriptContents)
        String initScriptPath = initScriptFile.absolutePath
        logger.info("using ${initScriptPath} as the path for the gradle init script")
        Executable executable = new Executable(sourceDirectory, gradleExecutable, [
            detectConfiguration.getGradleBuildCommand(),
            "--init-script=${initScriptPath}" as String
        ])
        executableRunner.execute(executable)

        File buildDirectory = new File(sourcePath, 'build')
        File blackduckDirectory = new File(buildDirectory, 'blackduck')

        File[] codeLocationFiles = detectFileManager.findFiles(blackduckDirectory, '*_detectCodeLocation.json')
        List<DetectCodeLocation> codeLocations = codeLocationFiles.collect { File file ->
            logger.debug("Code Location file name: ${file.getName()}")
            String codeLocationJson = file.getText(StandardCharsets.UTF_8.toString())
            gson.fromJson(codeLocationJson, DetectCodeLocation.class)
        }
        if (detectConfiguration.gradleCleanupBuildBlackduckDirectory) {
            blackduckDirectory.deleteDir()
        }
        codeLocations
    }
}