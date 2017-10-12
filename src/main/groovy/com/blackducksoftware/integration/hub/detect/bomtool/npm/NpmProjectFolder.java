/**
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
package com.blackducksoftware.integration.hub.detect.bomtool.npm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.blackducksoftware.integration.hub.detect.bomtool.NpmBomTool;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class NpmProjectFolder {
    private final String path;
    private final File nodeModulesDirectory;

    private NpmPackageJson packageJson = null;

    public NpmProjectFolder(final String path) {
        this.path = path;
        nodeModulesDirectory = new File(path, NpmBomTool.NODE_MODULES);
    }

    public NpmProjectFolder(final File projectDirectory) {
        this.path = projectDirectory.getPath();
        this.nodeModulesDirectory = new File(projectDirectory, NpmBomTool.NODE_MODULES);
    }

    public NpmProjectFolder getParentNpmProject() {
        File nodeModulesParent = nodeModulesDirectory.getParentFile();

        while (nodeModulesParent != null) {
            if (NpmBomTool.NODE_MODULES.equals(nodeModulesParent.getName())) {
                return new NpmProjectFolder(nodeModulesParent.getParentFile());
            }

            nodeModulesParent = nodeModulesParent.getParentFile();
        }

        return null;
    }

    public NpmProjectFolder getChildNpmProjectFromNodeModules(final String npmProjectName) {
        final File projectFolder = new File(nodeModulesDirectory, npmProjectName);
        if (projectFolder.exists()) {
            return new NpmProjectFolder(projectFolder);
        }

        return null;
    }

    public NpmPackageJson getPackageJson(final Gson gson) {
        if (packageJson == null) {
            try {
                final File packageJsonFile = new File(path, NpmBomTool.PACKAGE_JSON);
                final FileReader fileReader = new FileReader(packageJsonFile);
                final JsonReader jsonReader = new JsonReader(fileReader);
                final NpmPackageJson newPackageJson = gson.fromJson(jsonReader, NpmPackageJson.class);
                packageJson = newPackageJson;
            } catch (final FileNotFoundException e) {
                packageJson = new NpmPackageJson();
            }
        }

        return packageJson;
    }

    public String getPath() {
        return path;
    }

    public File getNodeModulesDirectory() {
        return nodeModulesDirectory;
    }
}