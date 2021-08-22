/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2021 Seanox Software Solutions
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

class Datasource {
    
    private final static Pattern PATTERN_EXPRESSION = Pattern.compile("^(?i)([a-z](?:[\\w\\-]*\\w){0,1})((?:\\[\\s*\\d+\\s*\\]){0,1})(\\.([a-z](?:[\\w\\-]*\\w){0,1})((?:\\[\\s*\\d+\\s*\\]){0,1}))*$");
    
    private final static Pattern PATTERN_LIST_EXPRESSION = Pattern.compile("^(.*)\\s*\\[\\s*(\\d+)\\s*\\]$");
    
    private final static Pattern PATTERN_KEY = Pattern.compile("^((\\w+\\[\\d+\\])|(\\w+))(\\.((\\w+\\[\\d+\\])|(\\w+)))*$");
    
    private final static Pattern PATTERN_KEY_DELIMITER = Pattern.compile("\\.");
    
    private static class NaturalComparator implements Comparator<String> {
        
        private static String normalize(final String string) {
            if (StringUtils.isBlank(string))
                return "";
            final StringBuilder buffer = new StringBuilder();
            for (final String fragment : string.trim().split("((?<=\\d)(?!\\d))|((?<!\\d)(?=\\d))")) {
                if (fragment.matches("^\\d+$")) {
                    final String patch = fragment.replaceAll("^0+", "");
                    buffer.append(Long.toString(patch.length(), 36).toUpperCase());
                    buffer.append(patch);
                } else buffer.append(fragment);
            }
            return buffer.toString();
        }

        @Override
        public int compare(final String string1, final String string2) {
            return NaturalComparator.normalize(string1)
                    .compareTo(NaturalComparator.normalize(string2));
        }
    }  
    
    static class ParserException extends Exception {
        
        private static final long serialVersionUID = 8942579089465571742L;

        ParserException(final String message) {
            super(message);
        }
    }
    
    static void collect(Map<String, Object> map, String key, CharSequence value)
            throws ParserException {
        
        Objects.requireNonNull(map);
        if (!PATTERN_EXPRESSION.matcher(key).find())
            throw new ParserException("Invalid key: " + key);
        if (!PATTERN_KEY.matcher(key).find())
            throw new ParserException("Invalid key: " + key);
        
        if (Objects.isNull(value))
            value = "";
        
        final List<String> entries = new ArrayList<>(Arrays.asList(PATTERN_KEY_DELIMITER.split(key)));
        while (entries.size() > 0) {
            String entry = entries.remove(0);
            if (PATTERN_LIST_EXPRESSION.matcher(entry).find()) {
                final int index = Integer.valueOf(PATTERN_LIST_EXPRESSION.matcher(entry).replaceAll("$2")).intValue();
                entry = PATTERN_LIST_EXPRESSION.matcher(entry).replaceAll("$1").trim();
                if (!(map.get(entry) instanceof List))
                    map.put(entry, new ArrayList<>());
                final List list = (List)map.get(entry);
                if (list.size() < index)
                    throw new ParserException("Invalid key index: " + key);
                if (entries.size() > 0) {
                    if (list.size() > index) {
                        if (!(list.get(index) instanceof Map))
                            list.set(index, new HashMap<>());
                    } else list.add(index, new HashMap<>());
                    map = (Map)list.get(index);
                } else {
                    if (list.size() > index)
                        list.set(index, value);
                    else list.add(index, new HashMap<>());
                }
            } else {
                if (entries.size() > 0) {
                    if (!(map.get(entry) instanceof Map))
                        map.put(entry, new HashMap<>());
                    map = (Map)map.get(entry);
                } else map.put(entry, value);
            }
        }
    }
    
    static <T> List<T> collect(final Class<T> type)
            throws Exception {

        final Properties properties = new Properties();
        properties.load(Datasource.class.getResourceAsStream("/data/" + Datasource.class.getSimpleName() + ".properties"));

        final Map<String, Object> map = new HashMap<>();
        final Set keySet = new TreeSet<>(new NaturalComparator());
        keySet.addAll(properties.keySet());
        for (final Object key : keySet) {
            final String source = ((String)key);
            final String target = source.replaceAll("(^\\.+)|(\\.+$)", "");
            Datasource.collect(map, target, properties.getProperty(source));
        }

        final String scope = StringUtils.uncapitalize(type.getSimpleName()) + "s";
        final List<T> collection = new ArrayList<>();
        final ObjectMapper mapper = new ObjectMapper();
        for (final Map<String, Object> entry : (List<Map<String, Object>>)map.get(scope))
            collection.add(mapper.convertValue(entry, type));
        
        return collection;
    }
}