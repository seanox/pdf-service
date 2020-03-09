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
package com.seanox.pdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/** 
 * Abstract class for implementing of template implementations.
 * 
 * <dir><b>About the templates</b></dir>
 * As engine {@link Generator} is used, here you can find more details.
 * The most important in short form:
 *  
 * <dir><code>#[palceholder]</code></dir>
 * Simple placeholder, global or in a section.
 *  
 * <dir><code>#[palceholder-exists]</code></dir>
 * Pendant to any placeholder, if it exists.
 *  
 * <dir><code>#[section[[...]]]</code></dir>
 * Section/Bock can contain more substructures.
 * Sections/blocks are only rendered if a corresponding map entry exists.
 *  
 * <dir><code>![static-text]</code></dir>
 * Placeholder for static text from the ResourceBundle.
 * As language {@link Service.Meta#locale} is used.
 *  
 * <dir><code>#[locale]</code></dir>
 * Placeholder provided by {@link Service} with the current language.
 * Available in all sections (header, content/data, footer).
 *  
 * <dir><code>#[page]</code></dir>
 * Placeholder provided by {@link Service} with the current page number.
 * Available in sections: header, footer
 *  
 * <dir><code>#[pages]</code></dir>
 * Placeholder provided by {@link Service} with the total page number.
 * Available in sections: header, footer
 *  
 * <dir><code>#[dataset]</code></dir>
 * Placeholder provided by {@link Service} with a collection of data objects.
 * Available in sections: content<br>
 * <br>
 * Template 3.1.0x 20200309<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.1.0x 20200309
 */
public abstract class Template extends Service.Template {
    
    /** Naturally sort comparator */
    private static class NaturalComparator implements Comparator<String> {
        
        /**
         * Normalizes the numeric fragments that they can be sorted.
         * @param  string string to be escaped
         * @return the normalized string
         */
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
    
    /**
     * Exception object for syntactic and structural errors in the properties
     * file of the preview data.
     */
    static class PreviewDataParserException extends Exception {
        
        private static final long serialVersionUID = 8942579089465571742L;

        /**
         * Constructor, creates a new PreviewDataParserException object.
         * @param message
         */
        PreviewDataParserException(String message) {
            super(message);
        }
    }
    
    /**
     * Creates a nested map structure for a data object, comparable to JSON.
     * The nesting is based on the dot as separator in the key.
     * The map structure supports three data types: Map, Collection, String.
     * Map and Collection are only used for nesting.
     * At the end a key with a string value must always be used.
     * Rules:
     *   - each dot in the key creates/uses a sub-map
     *   - if a (partial)key ends with [n], a list with a map is created/used
     *     n is the index in the list
     *      
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void collectPreviewData(Map<String, Object> map, String key, String value)
            throws PreviewDataParserException {

        final String PATTERN_EXPRESSION = "^(?i)([a-z](?:[\\w\\-]*\\w)*)((?:\\[\\s*\\d+\\s*\\])*)(\\.([a-z](?:[\\w\\-]*\\w)*)((?:\\[\\s*\\d+\\s*\\])*))*$";
        if (!key.matches(PATTERN_EXPRESSION))  
            throw new PreviewDataParserException("Invalid key: " + key);
        
        if (key.contains(".")) {
            String path = key.replaceAll("\\.[^\\.]+$", "");
            for (String entry : path.split("\\.")) {
                final String PATTERN_LIST_EXPRESSION = "^(.*)\\s*\\[\\s*(\\d+)\\s*\\]$";
                if (entry.matches(PATTERN_LIST_EXPRESSION)) {
                    int index = Integer.valueOf(entry.replaceAll(PATTERN_LIST_EXPRESSION, "$2")).intValue();
                    entry = entry.replaceAll(PATTERN_LIST_EXPRESSION, "$1").trim();
                    if (!map.containsKey(entry)
                            || !(map.get(entry) instanceof List))
                        map.put(entry, new ArrayList<>());
                    List list = (List)map.get(entry);
                    if (list.size() < index)
                        throw new PreviewDataParserException("Invalid key index: " + key);
                    if (list.size() > index) {
                        if (!(list.get(index) instanceof Map))
                            list.set(index, new HashMap<>());
                    } else list.add(index, new HashMap<>());
                    map = (Map)list.get(index);
                } else {
                    if (!map.containsKey(entry)
                            || !(map.get(entry) instanceof Map))
                        map.put(entry, new HashMap<>());
                    map = (Map)map.get(entry);
                    
                }
            }
            key = key.replaceAll("^.*\\.", "");
        }
        map.put(key, value);
    }

    /**
     * Creates preview data based on a properties file corresponding to impl.
     * The properties file are in the same package and use the same name.<br>
     * <dir>e.g. ArticleTemplateImpl -&gt; ArticleTemplateImpl.properties</dir>
     * @return the data for one data record as preview
     * @throws Exception
     *     In case of unexpected errors.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Map<String, Object> getPreviewData()
            throws Exception {
        
        Properties properties = new Properties();
        properties.load(this.getResourceStream("properties"));
        Map<String, Object> map = new HashMap<>();
        Set keySet = new TreeSet<>(new NaturalComparator());
        keySet.addAll(properties.keySet());
        for (Object key : keySet) {
            String source = ((String)key);
            String target = source.replaceAll("(^\\.+)|(\\.+$)", "");
            Template.collectPreviewData(map, target, properties.getProperty(source));
        }
        return map;
    }
    
    /**
     * Escapes all characters greater than ASCII 0x7F as HTML code.
     * The value {@code null} is used as a space.
     * @param  text text to escape
     * @return the possibly escaped text
     */
    static String escapeHtml(String text) {
        
        if (text == null)
            return "";
        StringBuilder build = new StringBuilder();
        for (char digit : text.toCharArray()) {
            if (digit > 0x7F)
                build.append("&#").append((int)digit).append(";");
            else if (digit == '&')
                build.append("&amp;");
            else build.append(digit);
        }
        text = build.toString();
        text = text.replaceAll("(\r\n)|(\n\r)|[\r\n]", "<br/>");
        return text;
    }
    
    /**
     * Escapes the text values of/in a Object.
     * @param  object object to escape
     * @return the escaped object
     */   
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object escapeHtml(Object object) {
        
        if (object == null)
            return "";
        if (object instanceof Collection)
            return Template.escapeHtml((Collection)object);
        if (object instanceof Map)
            return Template.escapeHtml((Map)object);
        return Template.escapeHtml(String.valueOf(object));
    } 

    /**
     * Escapes the text values in a Collection.
     * @param  collection collection with text values to escape
     * @return the Collection with escaped text values
     */
    private static Collection<Map<String, Object>> escapeHtml(Collection<Map<String, Object>> collection) {
        
        if (collection == null)
            collection = new ArrayList<>();
        return collection.stream().map(
                entry -> Template.escapeHtml(entry)
            ).collect(Collectors.toList());
    }
    
    /**
     * Escapes the text values in a Map.
     * @param  map map with text values to escape
     * @return the Map with escaped text values
     */
    private static Map<String, Object> escapeHtml(Map<String, Object> map) {
        
        if (map == null)
            map = new HashMap<>();
        return map.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> Template.escapeHtml(entry.getValue())
            ));        
    }
    
    /**
     * Extends the contained maps by an exists-key for each key.
     * This hack is necessary because CSS :empty has no effect in OpenHtmlToPdf
     * and empty elements cannot be smoothed out by CSS. Therefore the inverted
     * exists solution.
     * @param  collection
     * @return collection with additional exists keys
     */
    private static Collection<Map<String, Object>> indicateEmpty(Collection<Map<String, Object>> collection) {
        
        List<Map<String, Object>> result = new ArrayList<>();
        if (collection == null)
            collection = new ArrayList<>();
        collection.forEach(entry -> {
            if (!entry.isEmpty())
                result.add(Template.indicateEmpty(entry));
        });
        return result;
    }
    
    /**
     * Extends the maps by an exists-key for each key.
     * This hack is necessary because CSS :empty has no effect in OpenHtmlToPdf
     * and empty elements cannot be smoothed out by CSS. Therefore the inverted
     * exists solution.
     * @param  collection
     * @return collection with additional exists keys
     */    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, Object> indicateEmpty(Map<String, Object> map) {
        
        Map<String, Object> result = new HashMap<>();
        if (map == null)
            map = new HashMap<>();
        map.entrySet().forEach(entry -> {
            if (entry.getValue() != null) {
                if (entry.getValue() instanceof Collection) {
                    Collection value = (Collection)entry.getValue();
                    if (!value.isEmpty())
                        result.put(entry.getKey() + "-exists", "exists");
                    result.put(entry.getKey(), Template.indicateEmpty(value));
                } else if (entry.getValue() instanceof Map) {
                    Map value = (Map)entry.getValue();
                    if (!value.isEmpty())
                        result.put(entry.getKey() + "-exists", "exists");
                    result.put(entry.getKey(), Template.indicateEmpty(value));
                } else {
                    String value = String.valueOf(entry.getValue());
                    if (!value.trim().isEmpty())
                        result.put(entry.getKey() + "-exists", "exists");
                    result.put(entry.getKey(), value);
                }
            }
        });
        return result;
    }    

    @Override
    protected String generate(String markup, Service.Meta.Type type, Service.Meta meta) {

        Map<String, String> statics = meta.getStatics();
        if (statics == null)
            statics = new HashMap<>();
        Pattern pattern = Pattern.compile("\\!\\[\\s*(.*?)\\s*\\]");
        Matcher matcher = pattern.matcher(markup);
        while (matcher.find())
            markup = markup.replace(matcher.group(0), Template.escapeHtml(statics.get(matcher.group(1))));

        Generator generator = Generator.parse(markup.getBytes());
        if (Service.Meta.Type.HEADER.equals(type)) {
            Map<String, Object> header = Template.escapeHtml(meta.getHeader());
            header = Template.indicateEmpty(header);
            generator.set(type.toString(), header);
        } else if (Service.Meta.Type.DATA.equals(type)) {
            generator.set(new HashMap<String, Object>() {
                private static final long serialVersionUID = 1L; {
                Collection<Map<String, Object>> dataset = Template.escapeHtml(meta.getDataset());
                dataset = Template.indicateEmpty(dataset);
                put("dataset", dataset);
            }});            
        } else if (Service.Meta.Type.FOOTER.equals(type)) {
            Map<String, Object> footer = Template.escapeHtml(meta.getFooter());
            footer = Template.indicateEmpty(footer);
            generator.set(type.toString(), footer);
        }
        
        generator.set(new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L; {
            if (meta.getLocale() != null)
                put("locale", meta.getLocale().getLanguage());
        }});
        
        return new String(generator.extract());
    }
}