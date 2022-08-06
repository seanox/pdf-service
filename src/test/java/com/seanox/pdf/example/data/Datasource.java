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
package com.seanox.pdf.example.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Template;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class Datasource {

    static <T> List<T> collect(final Class<T> type)
            throws Exception {

        final var properties = new Properties();
        properties.load(Datasource.class.getResourceAsStream("/data/" + Datasource.class.getSimpleName() + ".properties"));
        final var map = (new Template() {
            protected Properties getPreviewProperties() {
                return properties;
            }
            protected Map<String, Object> getPreviewData()
                    throws Exception {
                return super.getPreviewData();
            }
        }).getPreviewData();

        final var scope = StringUtils.uncapitalize(type.getSimpleName()) + "s";
        final var collection = new ArrayList<T>();
        final var mapper = new ObjectMapper();
        for (final var entry : (List<Map<String, Object>>)map.get(scope))
            collection.add(mapper.convertValue(entry, type));
        
        return collection;
    }
}