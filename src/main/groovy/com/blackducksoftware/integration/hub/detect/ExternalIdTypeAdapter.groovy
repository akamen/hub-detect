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
package com.blackducksoftware.integration.hub.detect

import com.blackducksoftware.integration.hub.bdio.simple.model.Forge
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.MavenExternalId
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

import groovy.transform.TypeChecked

@TypeChecked
class ExternalIdTypeAdapter extends TypeAdapter<ExternalId> {
    Map<String, Forge> forgeMap = [cocoapods: Forge.COCOAPODS, maven: Forge.MAVEN, npm: Forge.NPM, pypi: Forge.PYPI, rubygems: Forge.RUBYGEMS]
    Gson gson = new Gson()

    @Override
    void write(final JsonWriter jsonWriter, final ExternalId value) throws IOException {
        final TypeAdapter defaultAdapter = gson.getAdapter(value.getClass())
        defaultAdapter.write(jsonWriter, value)
    }

    @Override
    ExternalId read(final JsonReader jsonReader) throws IOException {
        Forge forge = null
        final Map<String, String> otherProperties = [:]
        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            final String fieldName = jsonReader.nextName()
            if (fieldName.equals("forge")) {
                forge = readForge(jsonReader)
            } else {
                final String fieldValue = jsonReader.nextString()
                otherProperties.put(fieldName, fieldValue)
            }
        }
        jsonReader.endObject()

        if (Forge.MAVEN.equals(forge)) {
            return new MavenExternalId(otherProperties.get("group"), otherProperties.get("name"), otherProperties.get("version"))
        } else if (forgeMap.containsKey(forge.name)) {
            return new NameVersionExternalId(forge, otherProperties.get("name"), otherProperties.get("version"))
        }
        return null
    }

    private Forge readForge(JsonReader jsonReader) {
        String name = null
        String separator = null

        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            String propertyName = jsonReader.nextName()
            if (propertyName.equals('name')) {
                name = jsonReader.nextString()
            } else if (propertyName.equals('separator')) {
                separator = jsonReader.nextString()
            } else {
                jsonReader.skipValue()
            }
        }
        jsonReader.endObject()

        return new Forge(name, separator)
    }
}
