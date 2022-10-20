/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2022 Seanox Software Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.seanox.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Reader for test data.
 *
 * @author  Seanox Software Solutions
 * @version 1.0.0 20220806
 */
class TestDataReader {

    static String readTestContent(final String source)
            throws Exception {
        final var stackTraceElement = Arrays.stream(new Throwable().getStackTrace())
                .filter(fetch -> !TestDataReader.class.getName().equals(fetch.getClassName()))
                .findFirst().get();
        final var resourcePath = stackTraceElement.getClassName()
                .replaceAll("\\$.*$", "")
                .replaceAll("\\.[^\\.]+$", "")
                .replace('.', '/');
        final var className = StringUtils.uncapitalize(stackTraceElement.getClassName()
                .replaceAll("\\$.*$", "")
                .replaceAll("(^.*\\.)|(Test$)", ""));
        final var classLoader = Class.forName(stackTraceElement.getClassName()).getClassLoader();
        return new String(classLoader.getResourceAsStream(resourcePath + "/" + className + "/" + source).readAllBytes());
    }

    static Map<String, Object> readTestDataMap(final String source)
            throws Exception {
        return new ObjectMapper().readValue(TestDataReader.readTestContent(source), HashMap.class);
    }

    static Properties readTestDataProperties(final String source)
            throws Exception {
        final var dataString = TestDataReader.readTestContent(source);
        final var properties = new Properties();
        properties.load(new ByteArrayInputStream(dataString.getBytes()));
        return properties;
    }
}