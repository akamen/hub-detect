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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.dependency.Dependency;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.bomtool.npm.model.NpmTree;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.google.gson.Gson;

@Component
public class NpmDependencyFinder {

    @Autowired
    Gson gson;

    @Autowired
    ExternalIdFactory externalIdFactory;

    @Autowired
    DetectConfiguration detectConfiguration;

    public DetectCodeLocation createDependencyGraph(final String sourcePath) {
        final NpmTree tree; // = i dunno make tree
        final MutableDependencyGraph graph = new MutableMapDependencyGraph();
        traverse(tree, graph);
        return new DetectCodeLocation(BomToolType.NPM, sourcePath, "", "", null, graph);
    }

    private Dependency dependencyFromTree(final NpmTree tree) {
        final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPM, tree.getName(), tree.getVersion());
        final Dependency dependency = new Dependency(tree.getName(), tree.getVersion(), externalId);
        return dependency;
    }

    private void traverse(final NpmTree tree, final MutableDependencyGraph graph) {
        final Dependency dependency = dependencyFromTree(tree);
        for (final String child : tree.getDependencies()) {
            final NpmTree childTree = findTree(tree, child);
            final Dependency childDependency = dependencyFromTree(childTree);
            graph.addParentWithChild(dependency, childDependency);

            traverse(childTree, graph);
        }
    }

    private NpmTree findTree(final NpmTree tree, final String name) {
        for (final NpmTree child : tree.getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return findTree(tree.getParent(), name);
    }

}