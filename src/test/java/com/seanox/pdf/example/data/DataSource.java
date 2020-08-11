/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2020 Seanox Software Solutions
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

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

class Datasource {
    
    private final static Pattern PATTERN_EXPRESSION = Pattern.compile("^(?i)([a-z](?:[\\w\\-]*\\w){0,1})((?:\\[\\s*\\d+\\s*\\]){0,1})(\\.([a-z](?:[\\w\\-]*\\w){0,1})((?:\\[\\s*\\d+\\s*\\]){0,1}))*$");
    
    private final static Pattern PATTERN_LIST_EXPRESSION = Pattern.compile("^(.*)\\s*\\[\\s*(\\d+)\\s*\\]$");
    
    private final static Pattern PATTERN_KEY = Pattern.compile("^((\\w+\\[\\d+\\])|(\\w+))(\\.((\\w+\\[\\d+\\])|(\\w+)))*$");
    
    private final static Pattern PATTERN_KEY_DELIMITER = Pattern.compile("\\.");
    
    private static class NaturalComparator implements Comparator<String> {
        
        private static String normalize(String string) {
            
            String buffer = "";
            string = StringUtils.trimToEmpty(string);
            for (String fragment : string.split("(?:(?<=\\d)(?!\\d))|(?:(?<!\\d)(?=\\d))")) {
                try {
                    fragment = Long.valueOf(fragment).toString();
                    fragment = Long.toString(fragment.length(), 36).toUpperCase() + fragment;
                } catch (NumberFormatException exception) {
                }
                buffer += fragment;
            }
            return buffer;
        }

        @Override
        public int compare(String string1, String string2) {

            string1 = NaturalComparator.normalize(string1);
            string2 = NaturalComparator.normalize(string2);
            return string1.compareTo(string2);
        }
    }  
    
    static class ParserException extends Exception {
        
        private static final long serialVersionUID = 8942579089465571742L;

        ParserException(String message) {
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
        
        if (value == null)
            value = "";
        
        List<String> entries = new ArrayList<>(Arrays.asList(PATTERN_KEY_DELIMITER.split(key)));
        while (entries.size() > 0) {
            String entry = entries.remove(0);
            if (PATTERN_LIST_EXPRESSION.matcher(entry).find()) {
                int index = Integer.valueOf(PATTERN_LIST_EXPRESSION.matcher(entry).replaceAll("$2")).intValue();
                entry = PATTERN_LIST_EXPRESSION.matcher(entry).replaceAll("$1").trim();
                if (!map.containsKey(entry)
                        || !(map.get(entry) instanceof List))
                    map.put(entry, new ArrayList<>());
                List list = (List)map.get(entry);
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
                    if (!map.containsKey(entry)
                            || !(map.get(entry) instanceof Map))
                        map.put(entry, new HashMap<>());
                    map = (Map)map.get(entry);
                } else map.put(entry, value);
            }
        }
    }
    
    static <T> List<T> collect(Class<T> type)
            throws Exception {
        
        Properties properties = new Properties();
        properties.load(Datasource.class.getResourceAsStream("/" + Datasource.class.getName().replace(".", "/") + ".properties"));

        Map<String, Object> map = new HashMap<>();
        Set keySet = new TreeSet<>(new NaturalComparator());
        keySet.addAll(properties.keySet());
        for (Object key : keySet) {
            String source = ((String)key);
            String target = source.replaceAll("(^\\.+)|(\\.+$)", "");
            Datasource.collect(map, target, properties.getProperty(source));
        }
        
        String scope = StringUtils.uncapitalize(type.getSimpleName()) + "s";
        List<T> collection = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (Map<String, Object> entry : (List<Map<String, Object>>)map.get(scope))
            collection.add(mapper.convertValue(entry, type));
        
        return collection;
    }
}