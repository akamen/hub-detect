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
package com.blackducksoftware.integration.hub.detect.codelocation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;

public abstract class CodeLocationNameProvider {
    @Autowired
    protected DetectFileManager detectFileManager;

    public abstract String generateBomToolName(CodeLocationName codeLocationName);

    public abstract String generateScanName(CodeLocationName codeLocationName);

    public String cleanScanTargetPath(final CodeLocationName codeLocationName) {
        final String scanTargetPath = codeLocationName.getScanTargetPath();
        final String sourcePath = codeLocationName.getSourcePath();
        final String finalSourcePathPiece = detectFileManager.extractFinalPieceFromPath(sourcePath);
        String cleanedTargetPath = "";
        if (StringUtils.isNotBlank(scanTargetPath) && StringUtils.isNotBlank(finalSourcePathPiece)) {
            cleanedTargetPath = scanTargetPath.replace(sourcePath, finalSourcePathPiece);
        }

        return cleanedTargetPath;
    }

}
