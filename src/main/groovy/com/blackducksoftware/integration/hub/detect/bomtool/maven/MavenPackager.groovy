/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.detect.bomtool.maven

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode
import com.blackducksoftware.integration.hub.detect.DetectProperties
import com.blackducksoftware.integration.hub.detect.type.BomToolType
import com.blackducksoftware.integration.hub.detect.util.ProjectInfoGatherer
import com.blackducksoftware.integration.hub.detect.util.executable.Executable
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner

@Component
public class MavenPackager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass())

    @Autowired
    ProjectInfoGatherer projectInfoGatherer

    @Autowired
    ExecutableRunner executableRunner

    @Autowired
    DetectProperties detectProperties

    public List<DependencyNode> makeDependencyNodes(String sourcePath, String mavenExecutable) {
        final List<DependencyNode> projects = []

        File sourceDirectory = new File(sourcePath)

        def arguments = ["dependency:tree"]
        if (detectProperties.getMavenScope()?.trim()) {
            arguments.add("-Dscope=${detectProperties.getMavenScope()}")
        }
        final Executable mvnExecutable = new Executable(sourceDirectory, mavenExecutable, arguments)
        final ExecutableOutput mvnOutput = executableRunner.executeLoudly(mvnExecutable)

        final MavenOutputParser mavenOutputParser = new MavenOutputParser()
        projects.addAll(mavenOutputParser.parse(mvnOutput.standardOutput))

        if (detectProperties.getMavenAggregateBom() && !projects.isEmpty()) {
            final DependencyNode firstNode = projects.remove(0)
            projects.each { subProject ->
                firstNode.children.addAll(subProject.children)
            }
            projects.clear()
            projects.add(firstNode)
            firstNode.name = projectInfoGatherer.getDefaultProjectName(BomToolType.MAVEN, sourcePath, firstNode.name)
            firstNode.version = projectInfoGatherer.getDefaultProjectVersionName(firstNode.version)
        }

        return projects
    }
}