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
package com.blackducksoftware.integration.hub.detect.type

import groovy.transform.TypeChecked

@TypeChecked
enum ExecutableType {
    BASH([(OperatingSystemType.WINDOWS): 'bash.exe', (OperatingSystemType.LINUX): 'bash']),
    CONDA([(OperatingSystemType.WINDOWS): 'conda.exe', (OperatingSystemType.LINUX): 'conda']),
    CPAN([(OperatingSystemType.WINDOWS): 'cpan.bat', (OperatingSystemType.LINUX): 'cpan']),
    CPANM([(OperatingSystemType.WINDOWS): 'cpanm.bat', (OperatingSystemType.LINUX): 'cpanm']),
    DOCKER([(OperatingSystemType.WINDOWS): 'docker.exe', (OperatingSystemType.LINUX): 'docker']),
    GO([(OperatingSystemType.WINDOWS): 'go.exe', (OperatingSystemType.LINUX): 'go']),
    GO_DEP([(OperatingSystemType.WINDOWS): 'dep.exe', (OperatingSystemType.LINUX): 'dep']),
    GRADLE([(OperatingSystemType.WINDOWS): 'gradle.bat', (OperatingSystemType.LINUX): 'gradle']),
    GRADLEW([(OperatingSystemType.WINDOWS): 'gradlew.bat', (OperatingSystemType.LINUX): 'gradlew']),
    MVN([(OperatingSystemType.WINDOWS): 'mvn.cmd', (OperatingSystemType.LINUX): 'mvn']),
    MVNW([(OperatingSystemType.WINDOWS): 'mvnw.bat', (OperatingSystemType.LINUX): 'mvnw']),
    NPM([(OperatingSystemType.WINDOWS): 'npm.cmd', (OperatingSystemType.LINUX): 'npm']),
    NUGET([(OperatingSystemType.WINDOWS): 'nuget.exe', (OperatingSystemType.LINUX): 'nuget']),
    PEAR([(OperatingSystemType.WINDOWS): 'pear.bat', (OperatingSystemType.LINUX): 'pear']),
    PERL([(OperatingSystemType.WINDOWS): 'perl.bat', (OperatingSystemType.LINUX): 'perl']),
    PIP([(OperatingSystemType.WINDOWS): 'pip.exe', (OperatingSystemType.LINUX): 'pip']),
    PIP3([(OperatingSystemType.WINDOWS): 'pip3.exe', (OperatingSystemType.LINUX): 'pip3']),
    PYTHON([(OperatingSystemType.WINDOWS): 'python.exe', (OperatingSystemType.LINUX): 'python']),
    PYTHON3([(OperatingSystemType.WINDOWS): 'python3.exe', (OperatingSystemType.LINUX): 'python3']),
    REBAR3([(OperatingSystemType.WINDOWS): 'rebar3.exe', (OperatingSystemType.LINUX): 'rebar3']),
    VIRTUALENV([(OperatingSystemType.WINDOWS): 'virtualenv.exe', (OperatingSystemType.LINUX): 'virtualenv'])

    private Map<OperatingSystemType, String> osToExecutableMap = [:]

    private ExecutableType(Map<OperatingSystemType, String> osToExecutableMap) {
        this.osToExecutableMap.putAll(osToExecutableMap)
    }

    /**
     * If an operating system specific executable is not present, the linux executable, which could itself not be present, will be returned.
     */
    public String getExecutable(OperatingSystemType operatingSystemType) {
        String osSpecificExecutable = osToExecutableMap[operatingSystemType]
        if (osSpecificExecutable) {
            return osSpecificExecutable
        } else {
            return osToExecutableMap[OperatingSystemType.LINUX]
        }
    }
}
