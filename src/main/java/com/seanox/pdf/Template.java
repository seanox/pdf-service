/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
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

import com.seanox.pdf.Service.Meta;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** 
 * Abstract class for implementing of template implementations.
 * 
 * <h3>About the templates</h3>
 * As engine {@link Generator} is used, here you can find more details.
 * The most important in short form:
 *  
 * <code>#[placeholder]</code><br>
 * Placeholder in general, representing a value or a structure with the same
 * name.
 *  
 * <code>#[placeholder-exists]</code><br>
 * Pendant to any placeholder, if the value is not {@code null}, not empty and
 * not blank. Then the placeholder contains the value {@code exists}.
 * The exists-placeholder is supported because of backward compatibility, but
 * is deprecated and is replaced by the value structure and value disposable
 * structure.
 *
 * <code>#[placeholder{{...#[#]...}}</code><br>
 * TOOD:
 *
 * <code>#[structure[[...]]]</code><br>
 * Structures are complex nested constructs for the output of objects, lists,
 * folders and much more and work like templates. Structures are defined only
 * once and can then be reused anywhere using the simple placeholder of the
 * same name. They are rendered only if the corresponding object exists as a
 * value. Because the placeholders for inserting structures are preserved, they
 * can be used to build lists.
 *
 * <code>#[structure{{...}}]</code><br>
 * TODO:
 *
 * <code>![static-text]</code><br>
 * Placeholder for static non-structured text e.g. from the ResourceBundle.
 * 
 * <code>![static-text-exists]</code><br>
 * Pendant to any placeholder of static non-structured text, if the value is not
 * {@code null}, not empty and not blank. Then the placeholder contains the
 * value {@code exists}.
 *  
 * <code>#[locale]</code><br>
 * Placeholder provided by {@link Service} with the current language.
 * Available in all sections (header, content/data, footer).
 *  
 * <code>#[page]</code><br>
 * Placeholder provided by {@link Service} with the current page number.
 * Available in sections: header, footer
 *  
 * <code>#[pages]</code><br>
 * Placeholder provided by {@link Service} with the total page number.
 * Available in sections: header, footer<br>
 * <br>
 * Template 4.2.0 20220806<br>
 * Copyright (C) 2022 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
public abstract class Template extends Service.Template {

    /** Pattern fragment of an identifier */
    private final static String TEXT_PATTERN_IDENTIFIER = "([_a-zA-Z$](?:[\\w-$]*[\\w$])?)";

    /** Pattern fragment of a numeric index */
    private final static String TEXT_PATTERN_NUMERIC_INDEX = "(?:\\[\\s*(\\d+)\\s*\\])";

    /** Pattern fragment of a key */
    private final static String TEXT_PATTERN_KEY = "((" + TEXT_PATTERN_IDENTIFIER + TEXT_PATTERN_NUMERIC_INDEX + ")|(" + TEXT_PATTERN_IDENTIFIER + "))";

    /** Pattern for the detection of markup */
    private final static Pattern PATTERN_MARKUP_DETECTION = Pattern.compile("(?si).*(([<>].*(<\\s*/)|(/\\s*>))|(&#\\d+;)|(&#x[0-9a-f]+;)|(&[a-z]+;)).*");
    
    /** Pattern for the validation of expressions */
    private final static Pattern PATTERN_EXPRESSION = Pattern.compile("^(?i)" + TEXT_PATTERN_IDENTIFIER + "(" + TEXT_PATTERN_NUMERIC_INDEX + "?)(\\." + TEXT_PATTERN_IDENTIFIER + "(" + TEXT_PATTERN_NUMERIC_INDEX + "?))*$");
    
    /** Pattern for the validation of list expressions */
    private final static Pattern PATTERN_LIST_EXPRESSION = Pattern.compile("^(.*)\\s*" + TEXT_PATTERN_NUMERIC_INDEX + "$");
    
    /** Pattern for the detection of line breaks */
    private final static Pattern PATTERN_LINE_BREAKS = Pattern.compile("(\r\n)|(\n\r)|[\r\n]");

    /** Pattern for the detection of ampersand (non entity) */
    private final static Pattern PATTERN_AMPERSAND = Pattern.compile("(?i)&(?!#\\d+;)(?!#x[0-9a-f]+;)(?![a-z]+;)");
    
    /** Pattern for the validation of key */
    private final static Pattern PATTERN_KEY = Pattern.compile("^" + TEXT_PATTERN_KEY + "(\\." + TEXT_PATTERN_KEY +")*$");
    
    /** Pattern for splitting keys */
    private final static Pattern PATTERN_KEY_DELIMITER = Pattern.compile("\\.");
    
    /** Naturally sort comparator */
    private static class NaturalComparator implements Comparator<String>, Serializable {
        
        /**
         * Normalizes the numeric fragments that they can be sorted.
         * Normalize means to expand all numeric values by a prefix to their
         * numeric size (e.g. 10 == 0010, 100 > 0010, 100 > 0050, 100 > 50).
         * @param  string string to be normalized
         * @return the normalized string
         */
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
        PreviewDataParserException(final String message) {
            super(message);
        }
    }
    
    /** 
     * CharSequence for Markup.
     * {@link Markup} works like a {@link String}, but no HTML symbols are escaped.
     * It can be used if markup is to be inserted as value in the template.
     */
    public static class Markup implements CharSequence {
        
        private final String string;
        
        /**
         * Constructor, creates a new Markup object.
         * The value {@code null} is interpreted like an empty text.
         * @param text
         */
        public Markup(final CharSequence text) {
            this.string = Objects.nonNull(text) ? String.valueOf(text) : "";
        }
        
        @Override
        public int length() {
            return this.string.length();
        }

        @Override
        public char charAt(final int index) {
            return this.string.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return this.string.subSequence(start, start);
        }

        @Override
        public IntStream codePoints() {
            return this.string.codePoints();
        }

        @Override
        public IntStream chars() {
            return this.string.chars();
        }
        
        @Override
        public String toString() {
            return this.string;
        }
    }
    
    /**
     * Creates a nested map structure for a data object, comparable to JSON.
     * The nesting is based on the dot as separator in the key.
     * The data structure supports the data types:
     *     {@link Collection}, {@link Map}, {@link Markup}, Text.
     * {@link Collection} and {@link Collection} are only used for nesting.
     * Text is {@link Markup} if it contains HTML sequences:
     *     {@code <.../>}, {@code >...</} {@code  &...;}.<br>
     * {@link Markup} Markup indicates text where the escape of HTML symbols is
     * not required. At the end a key with a text value is always expected.<br>
     * <br>
     * Rules:
     * <ul>
     *   <li>
     *     Properties are used for data and statics<br>
     *     Data uses all data as a structured map<br>
     *     Statics used only non-structured keys, without dot and list index
     *   </li>
     *   <li>
     *     Key with dot is an indicator for structured data<br>
     *     Each dot in the key creates/uses a sub-map for the structured data map
     *   </li>
     *   <li>
     *     Keys / partial keys ending with [n] create a list with (sub-)map<br>
     *         as an example:<br>
     *     report.data[0].value = Value 1<br>
     *     report.data[1].value = Value 2<br>
     *     report.data[2].value = Value 3
     *   </li>
     * </ul>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void collectPreviewData(Map<String, Object> map, String key, CharSequence value)
            throws PreviewDataParserException {
        
        Objects.requireNonNull(map);
        if (!PATTERN_EXPRESSION.matcher(key).find())
            throw new PreviewDataParserException("Invalid key: " + key);
        if (!PATTERN_KEY.matcher(key).find())
            throw new PreviewDataParserException("Invalid key: " + key);
        
        if (Objects.isNull(value))
            value = "";
        if (PATTERN_MARKUP_DETECTION.matcher(value).find())
            value = new Markup(value);           
        
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
                    throw new PreviewDataParserException("Invalid key index: " + key);
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
    
    /**
     * Returns the preview data for the template as properties.
     * The properties file are in the same package and use the same name.<br>
     * <dir>e.g. ArticleTemplateImpl -&gt; ArticleTemplateImpl.properties</dir>
     * @return the preview for one data record as preview
     * @throws Exception
     *     In case of unexpected errors.
     */
    protected Properties getPreviewProperties()
            throws Exception {
        final String resource = this.getSourcePath()
                .replaceAll("[^\\\\/\\.]+$", "") + "properties";
        Properties properties = new Properties();
        properties.load(this.getResourceStream(resource));
        return properties;
    }

    /**
     * Loads preview data of a properties file, corresponding to a template.
     * The properties file are in the same package and use the same name.<br>
     * <dir>e.g. ArticleTemplateImpl -&gt; ArticleTemplateImpl.properties</dir>
     * @return the structured data for the preview
     * @throws Exception
     *     In case of unexpected errors.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Map<String, Object> getPreviewData()
            throws Exception {
        final Properties properties = this.getPreviewProperties();
        final Map<String, Object> map = new HashMap<>();
        final Set keySet = new TreeSet<>(new NaturalComparator());
        keySet.addAll(properties.keySet());
        for (final Object key : keySet) {
            final String source = ((String)key);
            final String target = source.replaceAll("(^\\.+)|(\\.+$)", "");
            Template.collectPreviewData(map, target, properties.getProperty(source));
        }
        return map;
    }
    
    /**
     * Loads preview statics of a properties file, corresponding to a template.
     * The properties file are in the same package and use the same name.<br>
     * <dir>e.g. ArticleTemplateImpl -&gt; ArticleTemplateImpl.properties</dir>
     * Statics only contain non-structured keys, without dot and list index. 
     * @return the flat static data for the preview
     * @throws Exception
     *     In case of unexpected errors.
     */
    @Override
    protected Map<String, String> getPreviewStatics()
            throws Exception {
        return this.getPreviewProperties().entrySet().stream()
                .filter(entry -> ((String)entry.getKey()).matches("[\\w-]+"))
                .collect(Collectors.toMap(
                        (entry) -> String.valueOf(entry.getKey()),
                        (entry) -> String.valueOf(entry.getValue()),
                        (existing, value) -> value));
    }

    /**
     * Returns the markup of the template.
     * Existing meta tags (#include) are resolved.
     * @return the markup of the template
     * @throws Exception
     *     In case of unexpected errors.
     */
    @Override
    protected String getMarkup()
            throws Exception {
        return this.resolveIncludes(this.getBasePath(), super.getMarkup(), new ArrayList<>());
    }
    
    /**
     * Escapes characters greater ASCII 0x7F, markup symbols and line breaks.
     * The value {@code null} is used as a space.
     * @param  text text to escape
     * @return the possibly escaped text
     */
    static String escapeHtml(final String text) {
        return Template.escapeHtml(text, false);
    }

    /**
     * Escapes characters greater ASCII 0x7F, markup symbols and line breaks.
     * The <li>markup</li> option specifies that the content contains markup.
     * In this case, only the ASCII characters greater 0x7F are escaped.
     * The value {@code null} is used as a space.
     * @param  text   text to escape
     * @param  markup {@code true} specifies that the content contains markup
     * @return the possibly escaped text
     */
    static String escapeHtml(final String text, final boolean markup) {
        if (Objects.isNull(text))
            return "";
        final StringBuilder buffer = new StringBuilder();
        for (final char digit : text.toCharArray()) {
            if (digit > 0x7F)
                buffer.append("&#").append((int)digit).append(";");
            else if (!markup) {
                if (digit == '&')
                    buffer.append("&amp;");
                else if (digit == '<')
                    buffer.append("&lt;");
                else if (digit == '>')
                    buffer.append("&gt;");
                else buffer.append(digit);
            } else buffer.append(digit);
        }
        final String result = PATTERN_AMPERSAND.matcher(buffer.toString()).replaceAll("&amp;");
        if (markup)
            return result;
        return PATTERN_LINE_BREAKS.matcher(result).replaceAll("<br/>");
    }
    
    /**
     * Escapes the text values of/in a Object.
     * @param  object object to escape
     * @return the escaped object
     */   
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object escapeHtml(final Object object) {
        if (Objects.isNull(object))
            return "";
        if (object instanceof Collection)
            return Template.escapeHtml((Collection)object);
        if (object instanceof Map)
            return Template.escapeHtml((Map)object);
        return Template.escapeHtml(String.valueOf(object), object instanceof Markup);
    } 

    /**
     * Escapes the text values in a Collection.
     * @param  collection collection with text values to escape
     * @return the Collection with escaped text values
     */
    private static Collection<Map<String, Object>> escapeHtml(final Collection<Map<String, Object>> collection) {
        if (Objects.isNull(collection))
            return new ArrayList<>();
        try {return collection.stream().map(Template::escapeHtml).collect(Collectors.toList());
        } catch (ClassCastException exception) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Escapes the text values in a Map.
     * @param  map map with text values to escape
     * @return the Map with escaped text values
     */
    private static Map<String, Object> escapeHtml(final Map<String, Object> map) {
        if (Objects.isNull(map))
            return new HashMap<>();
        try {
            return map.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> Template.escapeHtml(entry.getValue())
            ));
        } catch (ClassCastException exception) {
            return new HashMap<>();
        }
    }
    
    /**
     * Extends the contained maps by an exists-key for each key.
     * This hack is necessary because CSS :empty has no effect in OpenHtmlToPdf
     * and empty elements cannot be smoothed out by CSS. Therefore, the
     * inverted exists solution.
     * @param  collection
     * @return collection with additional exists keys
     */
    private static Collection<Map<String, Object>> indicateEmpty(final Collection<Map<String, Object>> collection) {
        if (Objects.isNull(collection))
            return new ArrayList<>();
        final List<Map<String, Object>> result = new ArrayList<>();
        collection.forEach(entry -> {
            if (entry.isEmpty())
                return;
            result.add(Template.indicateEmpty(entry));
        });
        return result;
    }
    
    /**
     * Extends the maps by an exists-key for each key, whose value is not empty,
     * not blank and not {@code null} . The value is then {@code exists}.
     * This hack is necessary because CSS :empty has no effect in OpenHtmlToPdf
     * and empty elements cannot be smoothed out by CSS. Therefore, the
     * inverted exists solution.
     * @param  map
     * @return collection with additional exists keys
     */    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> Map<String, T> indicateEmpty(final Map<String, T> map) {
        if (Objects.isNull(map))
            return new HashMap<>();
        final Map<String, T> result = new HashMap<>();
        map.entrySet().forEach(entry -> {
            if (Objects.isNull(entry.getValue()))
                return;
            if (entry.getValue() instanceof Collection) {
                final Collection value = (Collection)entry.getValue();
                if (!value.isEmpty())
                    result.put(entry.getKey() + "-exists", (T)"exists");
                try {result.put(entry.getKey(), (T)Template.indicateEmpty(value));
                } catch (ClassCastException exception) {
                    result.put(entry.getKey(), (T)String.valueOf(entry.getValue()));
                }
            } else if (entry.getValue() instanceof Map) {
                final Map value = (Map)entry.getValue();
                if (!value.isEmpty())
                    result.put(entry.getKey() + "-exists", (T)"exists");
                try {result.put(entry.getKey(), (T)Template.indicateEmpty(value));
                } catch (ClassCastException exception) {
                    result.put(entry.getKey(), (T)String.valueOf(entry.getValue()));
                }
            } else {
                final String value = String.valueOf(entry.getValue());
                if (!value.trim().isEmpty())
                    result.put(entry.getKey() + "-exists", (T)"exists");
                result.put(entry.getKey(), (T)value);
            }
        });
        return result;
    }    
    
    /**
     * Resolves meta directives #include in markup recursively.
     * @param  path
     * @param  markup
     * @param  stack
     * @return the markup with resolved includes
     * @throws Exception
     *     In case of unexpected errors.
     */
    private String resolveIncludes(String path, String markup, List<String> stack)
            throws Exception {
        final Pattern pattern = Pattern.compile("(?i)(?:^|(?<=[\r\n]))\\s*#include(?:(?:\\s+([^\r\n]*)\\s*((?=[\r\n])|$))|(?=\\s*$))");
        final Matcher matcher = pattern.matcher(markup);
        while (matcher.find()) {
            if (matcher.groupCount() < 1)
                throw new TemplateException("Invalid include found");
            String patch = matcher.group(1);
            patch = this.followIncludes(path, patch, stack);
            markup = markup.replace(matcher.group(0), patch);
        }
        return markup;
    }

    /**
     * Follows includes in markup recursively.
     * @param  path
     * @param  include
     * @param  stack
     * @return the markup with resolved includes
     * @throws Exception
     *     In case of unexpected errors.
     */
    private String followIncludes(String path, String include, List<String> stack)
            throws Exception {
        if (include.startsWith("/")
                || include.startsWith("\\"))
            include = Service.Template.normalizePath(include);
        else include = Service.Template.normalizePath("/" + path + "/" + include);
        if (stack.contains(include))
            throw new TemplateRecursionException();
        final List<String> recursions = new ArrayList<>(stack);
        recursions.add(include);
        if (Objects.isNull(this.getResource(include)))
            throw new TemplateResourceNotFoundException(include);
        final String markup = new String(this.getResourceStream(include).readAllBytes());
        try {return this.resolveIncludes(Service.Template.normalizePath(include + "/.."), markup, recursions);
        } catch (TemplateRecursionException exception) {
            throw new TemplateException("Recursion found in: " + this.getResource(include));
        }
    }    

    @Override
    protected String generate(String markup, Type type, Meta meta) {
        
        // Comparable behaviour to the generator:
        // - Placeholders are case-insensitive
        // - Placeholders are limited to the following characters: a-z A-Z 0-9 _-
        // - Placeholders must begin with a letter
        // - Placeholders cannot be inserted subsequently
        // - Placeholders without value are removed at the end

        final Map<String, String> statics = meta.getStatics();
        final Pattern pattern = Pattern.compile("!\\[\\s*(.*?)\\s*\\]");
        final Matcher matcher = pattern.matcher(markup);
        while (matcher.find()) {
            String value = null;
            if (matcher.group(0).matches("^(?i)!\\[[a-z]([\\w-]*\\w)?\\]$"))
                value = statics.get(matcher.group(1).toLowerCase());
            if (Objects.isNull(value))
                value = "";
            value = value.replaceAll("#(?=\\[)", "#[0x23]");
            markup = markup.replace(matcher.group(0), value);
        }

        final Generator generator = Generator.parse(markup.getBytes());
        generator.set(meta.getData());
        generator.set(new HashMap<>() {
            private static final long serialVersionUID = 1L; {
            if (Objects.nonNull(meta.getLocale()))
                put("locale", meta.getLocale().getLanguage());
        }});
        
        return new String(generator.extract());
    }

    @Override
    protected byte[] render(Meta meta)
            throws Exception {
        
        if (Objects.isNull(meta))
            meta = new Meta();
        
        // Preparation/customization of the meta-object before rendering.
        // The rendering is done in three steps (content, header, footer) and so
        // this can be done once for all steps.

        Map<String, Object> data = meta.getData();
        data = Template.escapeHtml(data);
        data = Template.indicateEmpty(data);

        Map<String, String> statics = meta.getStatics();
        if (Objects.isNull(statics))
            statics = new HashMap<>();
        statics = statics.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .collect(Collectors.toMap(
                        (entry) -> entry.getKey().toLowerCase(),
                        (entry) -> Template.escapeHtml(entry.getValue(),
                                PATTERN_MARKUP_DETECTION.matcher(entry.getValue()).find()),  
                        (existing, value) -> value));
        statics = Template.indicateEmpty(statics);

        return super.render(new Meta(meta.getLocale(), data, statics));
    }
}