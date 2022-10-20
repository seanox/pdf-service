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

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Service.Template.TemplateException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Static service for creating PDF based on templates and meta-objects.
 *
 * <h3>Examples of use:</h3>
 * <pre>
 *   Service.render(template, meta);
 *    
 *   Files.write(Paths.get(template + ".pdf"), Service.render(template, meta),
 *         StandardOpenOption.CREATE);
 * </pre>
 *  
 * <h3>How it works:</h3>
 * The creation of PDFs is based on an HTML-to-PDF converter (openhtmltopdf).
 * In the first step, an HTML is created that contains all data records.
 * The HTML is based on a markup template with placeholders. A generator or
 * renderer fills the placeholders in the template and creates a
 * Single-Page-HTML as text.
 * The HTML-to-PDF converter creates the PDF from the HTML.
 * The pages are separated by CSS.

 * The service works data neutral. There is no special data object.
 * Only the key value entries in the maps and the placeholders in the template
 * determine the content.
 *  
 * <h3>Useful information:</h3>
 * Templates are based on an implementation of the {@link Template} and the
 * annotation {@link Resources}, which with {@link Resources#base()} and
 * {@link Resources#template()}) contains information about the base directory
 * of the resources (CSS, images, fonts, ...), as well the path of the markup
 * template with the same name.
 * It is practical if the template implementation, the markup template and any
 * template extensions (properties, ...) are stored in the same package.
 * 
 * The resources (CSS, images, fonts, ...) use HTML-to-PDF from the ClassPath,
 * means the base URI required by the HTML-to-PDF converter refers to the
 * ClassPath of this class. The location in the ClassPath can be defined with
 * {@link Resources#base()}.
 *
 * <h3>About the templates</h3>
 * As engine {@link Generator} is used, here you can find more details.
 * The most important in short form:
 *
 * Placeholders support values, structures and static texts. The identifier is
 * case-insensitive and based on the conventions of Java variables. Thus, the
 * identifier begins with one of the following characters: a-z A-Z _ $ and ends
 * on a word character: 0-9 a-z A-Z _ or $. In between, all word characters 0-9
 * a-z A-Z _ as well as the currency symbol ($) and the minus sign can be used.
 *
 * <b>Value Placeholder</b><br>
 * <code>#[identifier]</code><br>
 * Placeholders represent a value to the corresponding key of a level of a
 * structured or branched dictionary with key-value pairs. If to the identifier
 * a structure with the same name exists, this is applied to the value.
 *
 * <b>Structure Placeholder</b><br>
 * <code>#[identifier[[...]]]</code><br>
 * Structures are complex nested constructs for the output of values, nested
 * data structures as well as lists and function like templates. Structures are
 * defined once and can then be reused anywhere with simple placeholders of the
 * same identifier. They are rendered only if the key-value dictionary at the
 * appropriate level contains a key matching the identifier. For the data type
 * Collection and Map, the placeholders remain after rendering and can thus be
 * (re)used iteratively for lists or recursive for complex nested outputs.
 *
 * <b>Disposable Structure Placeholder</b><br>
 * <code>#[identifier{{...}}]</code><br>
 * Disposable structures are bound to their place and, unlike a normal
 * structure, can be defined differently multiple times, but cannot be reused
 * or extracted based on their identifier.
 *
 * <b>Disposable Value Placeholder</b><br>
 * <code>#[identifier{{... #[#] ...}}]</code><br>
 * The disposable structure placeholder can also be used for a value, which is
 * then represented by the placeholder {@code #[#]}.
 *
 * <code>#[identifier{{...}}]</code><br>
 * Also, a conditional output of text instead of the value is possible.
 *
 * <code>#[identifier{{... #[#] ... ![identifier] ...}}]</code><br>
 * Or the combination with static placeholders is also supported.
 *
 * <b>Exists Placeholder</b><br>
 * As part of the runtime placeholders the exits placeholder is generated
 * automatically based on the keys and values. In the meantime this function is
 * deprecated, because this is replaced by disposable structures placeholder.
 *
 * For each key and placeholder an exists-placeholder is provided. This can be
 * used in the markup and in combination with CSS to output/display markup
 * depending on the existence of values in meta-data. The Exists placeholder
 * contains the value `exists` if the value is not `null`, not empty and not
 * blank.
 *
 * <b>Escaped Placeholders</b><br>
 * <code>#[0x...]</code><br>
 * For the output of special and control characters a hexadecimal escape
 * sequence can be used, for which the identifier from the placeholder must
 * start with {@code 0x} and is followed by the hexadecimal code sequence.
 *
 * <b>Runtime Placeholder</b><br>
 * Runtime placeholders are additional automatically generated placeholders
 * based on the keys and values in the key-value dictionary.
 *
 * The placeholder {@code #[locale]} is provided from the meta-locale and can
 * be used for internationalization (i18n).
 *
 * The placeholders {@code #[page]} and {@code #[pages]} are available in the
 * header and footer and contain the current page and total number of pages.
 *
 * For each key and placeholder an exists-placeholder is provided. This can be
 * used in the markup and in combination with CSS to output/display markup
 * depending on the existence of values in meta-data. The Exists placeholder
 * contains the value {@code exists} if the value is not {@code null}, not
 * empty and not blank.
 *
 * <b>Static Placeholder</b><br>
 * <code>![identifier]</code><br>
 * For the output of static texts from meta-statics, which uses a strictly flat
 * and string-based key-value map without collections and branching. If no
 * value exists for a placeholder, it is removed.
 *
 * The static texts are practical, then key-value dictionary has no levels and
 * the keys can be used in any level of the template structures.
 *
 * <b>Static Exists Placeholder</b><br>
 * Analogous to the exists placeholder from the group of runtime placeholders,
 * there is also one for static texts.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
public class Service {
    
    /**
     * Creates a PDF for a template and data as meta-object.
     * @param  template {@link Template}
     * @param  meta     {@link Meta}
     * @return the created PDF as byte array
     * @throws TemplateException
     *     In case of unexpected errors.
     * @throws ServiceException
     *     In case of unexpected errors.
     */
    public static byte[] render(final Class<? extends Template> template, final Meta meta)
            throws ServiceException {

        final Template instance;
        try {instance = Service.Template.instantiate(template);
        } catch (Exception exception) {
            throw new Template.TemplateException(exception);
        }        
        
        try {return Service.render(instance, meta);
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }   
    
    /**
     * Creates a PDF for a template and data as meta-object.
     * @param  template {@link Template}
     * @param  meta     {@link Meta}
     * @return the created PDF as byte array
     * @throws ServiceException
     *     In case of unexpected errors.
     */
    public static byte[] render(final Template template, final Meta meta)
            throws ServiceException {
        
        try {new URI(template.getBase().toString());
        } catch (Exception exception) {
            throw new Template.TemplateException("Invalid base URI", exception);
        }

        try {return template.render(meta);
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Meta-object for creating PDFs.
     * The PDF creation is based on templates and is decoupled from the business
     * logic. The templates only know placeholders and structures.
     * Templates consist of the fragments: header, data and footer, which all
     * use the data from the meta-object.
     * The following data are supported: locale for internationalization (i18n),
     * structured data and static texts.<br>
     * <br>
     * The structured data is a map structure for a data object, comparable to
     * JSON. The data structure supports the data types: {@link Collection},
     * {@link Map}, Text. {@link Collection} and {@link Map} are only used for
     * nesting. At the end a key with a text value is always expected.<br>
     * <br>
     * Statics text are a flat map with text-based key-value pairs and is used
     * for flat, non-structured placeholders.
     */
    public static class Meta {

        /** locale */
        private Locale locale;

        /** key-value map for the data */
        private Map<String, Object> data;
        
        /** key-value map for the static texts */
        private Map<String, String> statics;

        /** Constructor, creates a new Meta object. */
        public Meta() {
        }

        /** 
         * Constructor, creates a new Meta object.
         * @param locale
         */
        public Meta(final Locale locale) {
            this.locale = locale;
        }
        
        /** 
         * Constructor, creates a new Meta object.
         * @param data
         */
        public Meta(final Map<String, Object> data) {
            this.data = data;
        }

        /** 
         * Constructor, creates a new Meta object.
         * @param data
         * @param statics
         */
        public Meta(final Map<String, Object> data, final Map<String, String> statics) {
            this.data = data;
            this.statics = statics;
        }
        
        /** 
         * Constructor, creates a new Meta object.
         * @param locale
         * @param data
         */
        public Meta(final Locale locale, final Map<String, Object> data) {
            this.locale = locale;
            this.data = data;
        }

        /** 
         * Constructor, creates a new Meta object.
         * @param locale
         * @param data
         * @param statics
         */
        public Meta(final Locale locale, final Map<String, Object> data, final Map<String, String> statics) {
            this.locale = locale;
            this.data = data;
            this.statics = statics;
        }

        /**
         * Return value of locale.
         * @return value of locale
         */
        public Locale getLocale() {
            return this.locale;
        }

        /**
         * Set value of locale.
         * @param locale value of locale
         */
        public void setLocale(final Locale locale) {
            this.locale = locale;
        }

        /**
         * Return value of data.
         * @return value of data
         */
        public Map<String, Object> getData() {
            return this.data;
        }

        /**
         * Set value of data.
         * @param data value of data
         */
        public void setData(final Map<String, Object> data) {
            this.data = data;
        }

        /**
         * Return value of statics.
         * @return value of statics
         */
        public Map<String, String> getStatics() {
            return this.statics;
        }

        /**
         * Set value of statics.
         * @param statics value of statics
         */
        public void setStatics(final Map<String, String> statics) {
            this.statics = statics;
        }

        @Override
        protected Meta clone() {
            final var meta = new Meta();
            meta.locale = this.locale;
            meta.data = this.data;
            if (Objects.nonNull(this.statics))
                meta.statics = SerializationUtils.clone(new HashMap<>(this.statics));
            if (Objects.nonNull(this.data))
                meta.data = SerializationUtils.clone(new HashMap<>(this.data));
            return meta;
        }
    }

    /** 
     * Abstract class for implementing templates.
     * The implementation defines the resource management, the preparation of
     * markup, the generation of markup and can optionally also define the PDF
     * rendering. The service only uses the API and has no own template and
     * markup generator.
     */
    public static abstract class Template {

        static Template instantiate(final Class<? extends Template> template)
                throws Exception {
            final var constructor = template.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }

        /**
         * Templates are based on an implementation of the
         * {@link Template} and the annotation {@link Resources}, which with
         * {@link Resources#base()} and {@link Resources#template()}) contains 
         * information about the base directory of the resources (CSS, images,
         * fonts, ...), as well the path of the markup template with the same
         * name.
         */
        @Documented
        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Resources {

            /** 
             * Base URI of the resources in the ClassPath.
             * Default value is the root in the ClassPath.
             */
            String base() default "/";

            /** 
             * Path of the markup template in the ClassPath.
             * Default value is path of the class in the ClassPath with the
             * extension 'html'.
             */
            String template() default "";
        }
        
        /** Array of template implementations detected in the ClassPath */
        private static Class<Template>[] templates;
        
        /**
         * Detects all template implementations in the ClassPath.
         * The detection is based on using the annotation {@link Resources} and
         * the implementation of {@link Template}.
         * The detection is time-consuming and is therefore only executed once
         * at runtime and the result is cached.
         * @return the detected template implementations as an array
         * @throws Exception
         *     In case of unexpected errors.
         */
        @SuppressWarnings("unchecked")
        public static Class<Template>[] scan()
                throws Exception {

            if (Objects.nonNull(Template.templates))
                return Template.templates.clone();
            
            final var templates = new ArrayList<>();
            for (final var basePackage : Stream.of(new Throwable().getStackTrace())
                    .map(element -> element.getClassName().replaceAll("\\W.*$", ""))
                    .distinct()
                    .toArray(String[]::new)) {
                final var provider = new ClassPathScanningCandidateComponentProvider(false);
                provider.addIncludeFilter(new AnnotationTypeFilter(Resources.class));
                for (final var beanDefinition : provider.findCandidateComponents(basePackage)) {
                    final var template = Class.forName(beanDefinition.getBeanClassName());
                    if (Template.class.isAssignableFrom(template))
                        templates.add(template);
                }
            }
            Template.templates = templates.toArray(new Class[0]);

            return Template.templates.clone();
        }

        /**
         * Returns the path of resources (CSS, images, fonts, ...).
         * @return the path of resources
         */
        protected String getBasePath() {
            final var resource = this.getClass().getAnnotation(Resources.class);
            if (Objects.isNull(resource)
                    || resource.base().trim().isEmpty())
                return "/";
            return resource.base().trim();
        }  
        
        /**
         * Returns the URI of resources path (CSS, images, fonts, ...).
         * @return the URI of resources path
         * @throws TemplateResourceNotFoundException
         *     If the path of resources cannot be found in the ClassPath.
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected URI getBase()
                throws Exception {
            return this.getResource(this.getBasePath());
        }
        
        /**
         * Returns the URI of the markup template.
         * @return the URI of the markup template
         * @throws TemplateResourceNotFoundException
         *     If the template cannot be found in the ClassPath.
         */
        protected String getSourcePath() {
            var template = "/" + this.getClass().getName().replace('.', '/') + ".html";
            final var resource = this.getClass().getAnnotation(Resources.class);
            if (StringUtils.isNotEmpty(resource.template())) {
                template = resource.template().trim();
                if (!template.startsWith("/"))
                    template = "/" + template;
            }
            return template;
        }        
        
        /**
         * Returns the URI of the markup template.
         * @return the URI of the markup template
         * @throws TemplateResourceNotFoundException
         *     If the template cannot be found in the ClassPath.
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected URI getSource()
                throws Exception {
            return this.getResource(this.getSourcePath());
        }

        /**
         * Returns the {@link InputStream} to the markup template.
         * @return the {@link InputStream} to the markup template
         * @throws TemplateResourceNotFoundException
         *     If the source cannot be found in the ClassPath.
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected InputStream getSourceStream()
                throws Exception {
            return this.getResourceStream(this.getSourcePath());
        }
        
        /**
         * Returns the URI of a resource in the ClassPath.
         * @param  resource
         * @return the URI of a resource in the ClassPath
         * @throws TemplateResourceNotFoundException
         *     If the resource cannot be found in the ClassPath.
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected URI getResource(final String resource)
                throws Exception {
            if (StringUtils.isEmpty(resource))
                throw new TemplateResourceNotFoundException();
            if (Objects.isNull(Service.class.getResource(resource)))
                throw new TemplateResourceNotFoundException(resource);
            return Service.class.getResource(resource).toURI();
        }

        /**
         * Returns the {@link InputStream} for a resource in the ClassPath.
         * @param  resource
         * @return the {@link InputStream} for a resource in the ClassPath
         * @throws TemplateResourceNotFoundException
         *     If the resource cannot be found in the ClassPath.
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected InputStream getResourceStream(final String resource)
                throws Exception {
            if (StringUtils.isEmpty(resource))
                throw new TemplateResourceNotFoundException();
            if (Objects.isNull(Service.class.getResource(resource)))
                throw new TemplateResourceNotFoundException(resource);
            return Service.class.getResourceAsStream(resource);
        }
        
        /**
         * Returns the markup of the template.
         * @return the markup of the template
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected String getMarkup()
                throws Exception {
            return new String(this.getSourceStream().readAllBytes());
        }

        /**
         * Returns the data for one data record as preview.
         * @return the data for one data record as preview
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected abstract Map<String, Object> getPreviewData()
                throws Exception;

        /**
         * Returns the static data for the preview.
         * @return the static data for the preview
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected abstract Map<String, String> getPreviewStatics()
                throws Exception;        
        
        /**
         * Creates the PDF as preview.
         * @return the PDF as preview
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected byte[] getPreview()
                throws Exception {
            return this.render(new Meta() {{
                this.setLocale(Locale.getDefault());
                this.setData(Template.this.getPreviewData());
                this.setStatics(Template.this.getPreviewStatics());
            }});
        }
        
        /**
         * The template can contain three fragments: header, content, footer.
         * The rendering of the markup uses overlays.
         * During rendering, the type tells the generator which fragment of the
         * markup is generated. The generator can thus react specifically to the
         * fragments if it is necessary for the generator implementation.
         * Type provides the constants for the layers as enumeration.
         */
        public enum Type {

            /** Meta-Type HEADER */
            HEADER,

            /** Meta-Type DATA */
            DATA,

            /** Meta-Type FOOTER */
            FOOTER
        }

        /**
         * Creates (X)HTML markup for the PDF based of data in a meta-object.
         * @param  markup
         * @param  type
         * @param  meta
         * @return the created (X)HTML markup for the PDF creation
         */
        protected abstract String generate(final String markup, final Type type, final Meta meta);
        
        /**
         * Merges a collection of PDDocuments into one. 
         * @param  documents
         * @return the merged PDDocuments
         * @throws IOException
         */
        protected static PDDocument merge(final Collection<PDDocument> documents)
                throws IOException {
            final var merge = new PDFMergerUtility();
            for (final var document : documents) {
                final var output = new ByteArrayOutputStream();
                document.save(output);
                merge.addSource(new ByteArrayInputStream(output.toByteArray()));
            }
            final var output = new ByteArrayOutputStream();
            merge.setDestinationStream(output);
            merge.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            return PDDocument.load(output.toByteArray());
        }
        
        /**
         * Normalizes a path.
         * Relative directives like . and .. are balanced out.
         * If necessary, backslashes are uniformly converted to slash.
         * The return value contains at least one slash.
         * @param  path
         * @return the normalizes a path
         */
        protected static String normalizePath(final String path) {
            
            String string;
            String stream;
            
            int    cursor;

            // path is changed to slash
            string = path.replace('\\', '/').trim();

            // multiple slashes are combined
            while ((cursor = string.indexOf("//")) >= 0)
                string = string.substring(0, cursor).concat(string.substring(cursor +1));

            // path is balanced if necessary /abc/./def/../ghi -> /abc/ghi
            // path is balanced by /.
            if (string.endsWith("/."))
                string = string.concat("/");

            while ((cursor = string.indexOf("/./")) >= 0)
                string = string.substring(0, cursor).concat(string.substring(cursor +2));

            // path is balanced by /..
            if (string.endsWith("/.."))
                string = string.concat("/");

            while ((cursor = string.indexOf("/../")) >= 0) {
                stream = string.substring(cursor +3);
                string = string.substring(0, cursor);

                cursor = string.lastIndexOf("/");
                cursor = Math.max(0, cursor);
                string = string.substring(0, cursor).concat(stream);
            }

            // multiple slashes are combined
            while ((cursor = string.indexOf("//")) >= 0)
                string = string.substring(0, cursor).concat(string.substring(cursor +1));

            // Paths without a slash at the beginning must remain without a
            // slash. This can happen when normalizing .. to the root.
            if (!path.replace('\\', '/').trim().startsWith("/")
                    && string.equals("/"))
                string = "";

            return string;
        }
        
        /**
         * Creates the PDF based on the data records as meta-object.
         * @param  meta data records as map array
         * @return the created PDF as byte array
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected byte[] render(Meta meta)
                throws Exception {

            if (Objects.isNull(meta))
                meta = new Meta();
            else meta = meta.clone();

            // Data is copied because it is manipulated later for the header and
            // footer by adding the keys and values for locale, page and pages.
            if (Objects.isNull(meta.data))
                meta.data = new HashMap<>();
            else meta.data = new HashMap<>(meta.data);

            var base = this.getBase();
            if (Objects.isNull(base.getScheme())) {
                if (Objects.isNull(Service.class.getResource(base.toString())))
                    throw new TemplateResourceNotFoundException(base.toString());
                base = Service.class.getResource(base.toString()).toURI();
            }
            if (!base.toString().endsWith("/"))
                base = new URI(base + "/");

            final var markup = this.getMarkup();
            final var multiplex = Multiplex.demux(markup);

            final var artifacts = new ArrayList<PDDocument>();
            
            try {

                PdfRendererBuilder builder;

                final var content = new ByteArrayOutputStream();
                builder = new PdfRendererBuilder();
                builder.withHtmlContent(this.generate(multiplex.content, Type.DATA, meta), base.toString());
                builder.toStream(content);
                builder.run();
                artifacts.add(PDDocument.load(content.toByteArray()));
                
                // without header and footer no overlay and merging is necessary
                // the byte array from the document can be returned
                if ((Objects.isNull(multiplex.header)
                                || multiplex.header.trim().isEmpty())
                        && (Objects.isNull(multiplex.footer)
                                || multiplex.footer.trim().isEmpty()))
                    return content.toByteArray();
                    
                try (final var document = Template.merge(artifacts)) {
                    final var splitter = new Splitter();
                    final var pages = splitter.split(document);
                    meta.data.put("pages", String.valueOf(pages.size()));
                    
                    for (var page : new ArrayList<>(pages)) {
                        var offset = pages.indexOf(page);
                        meta.data.put("page", String.valueOf(offset +1));
                        
                        if (Objects.nonNull(multiplex.header)
                                && !multiplex.header.trim().isEmpty()) {
                            final var header = new ByteArrayOutputStream();
                            builder = new PdfRendererBuilder();
                            builder.withHtmlContent(this.generate(multiplex.header, Type.HEADER, meta), base.toString());
                            builder.toStream(header);
                            builder.run();
                            
                            try (final var overlay = new Overlay()) {
                                overlay.setInputPDF(page);
                                overlay.setAllPagesOverlayPDF(PDDocument.load(header.toByteArray()));
                                ByteArrayOutputStream output = new ByteArrayOutputStream();
                                overlay.overlay(new HashMap<>()).save(output);
                                page.close();
                                page = PDDocument.load(output.toByteArray());
                            }
                        }

                        if (Objects.nonNull(multiplex.footer)
                                && !multiplex.footer.trim().isEmpty()) {
                            final var footer = new ByteArrayOutputStream();
                            builder = new PdfRendererBuilder();
                            builder.withHtmlContent(this.generate(multiplex.footer, Type.FOOTER, meta), base.toString());
                            builder.toStream(footer);
                            builder.run();  
                            
                            try (final var overlay = new Overlay()) {
                                overlay.setInputPDF(page);
                                overlay.setAllPagesOverlayPDF(PDDocument.load(footer.toByteArray()));
                                ByteArrayOutputStream output = new ByteArrayOutputStream();
                                overlay.overlay(new HashMap<>()).save(output);
                                page.close();
                                page = PDDocument.load(output.toByteArray());
                            }
                        }
                        
                        pages.add(offset +1, page);
                        pages.remove(offset).close();
                    }
                    
                    try (final var release = Template.merge(pages)) {
                        final var output = new ByteArrayOutputStream();
                        release.save(output);
                        return output.toByteArray();
                    } finally {
                        for (final var page : pages)
                            page.close();
                    }
                }

            } finally {
                for (final var artifact : artifacts)
                    artifact.close();
            }
        }
         
        @Override
        public String toString() {
            return this.getSourcePath();
        }

        /**
         * The template can contain three fragments: header, content, footer.
         * The content can be generated in one step. Because the content is
         * framed in a page and the page can contain margins in which headers
         * and footers must be inserted. Header and footer must therefore be
         * inserted later as an overlay. Header and footer can contain their
         * own placeholders and are therefore generated separately. So the
         * special placeholders page (current page number) and pages (total
         * number) can also be used.
         *
         * The multiplexer separates the markup for the fragments: header,
         * content, footer into completely separate (X)HTML documents. So that
         * three templates can be created from one template and each fragment
         * can be rendered individually as a PDF.
         * 
         * <h3>Header</h3>
         * The complete (X)HTML document with {@code BODY > HEADER} only.
         * 
         * <h3>Content</h3>
         * The complete (X)HTMl document without {@code BODY > HEADER} and
         * without {@code BODY > FOOTER}.
         * 
         * <h3>Footer</h3>
         * The complete (X)HTML document with {@code BODY > FOOTER} only.
         */
        protected static class Multiplex {

            /** markup of header */
            private String header;

            /** markup of content */
            private String content;

            /** markup of footer */
            private String footer;
            
            /**
             * Return value of header.
             * @return value of header
             */
            public String getHeader() {
                return this.header;
            }

            /**
             * Return value of content.
             * @return value of content
             */
            public String getContent() {
                return this.content;
            }

            /**
             * Return value of footer.
             * @return value of footer
             */
            public String getFooter() {
                return this.footer;
            }

            /**
             * Creates a list of nodes from a NodeList.
             * @param  nodes
             * @return list of nodes
             */
            private static List<Node> convertNodeList(final NodeList nodes) {
                
                if (Objects.isNull(nodes)
                        || nodes.getLength() == 0)
                    return Collections.emptyList();
                
                final var list = new ArrayList<Node>();
                for (var loop = 0; loop < nodes.getLength(); loop++)
                    list.add(nodes.item(loop));
                return list;
            } 

            /**
             * Creates a copy of the passed document.
             * @param  document
             * @return a copy of the passed document
             * @throws ParserConfigurationException
             */
            private static Document documentClone(final Document document)
                    throws ParserConfigurationException {
                final var root = document.getDocumentElement();
                final var factory = DocumentBuilderFactory.newInstance();
                final var builder = factory.newDocumentBuilder();
                final var clone = builder.newDocument();
                clone.appendChild(clone.importNode(root, true));
                return clone;
            }

            /**
             * Fetch a node to a selector.
             * If the selector matches multiple nodes, it will return the first. 
             * @param  document
             * @param  selector
             * @return the first matching node, otherwise {@code null}
             * @throws XPathExpressionException
             */
            private static Node documentFetchNode(final Document document, final String selector)
                    throws XPathExpressionException {
                final var xpath = XPathFactory.newInstance().newXPath();
                final var expression = xpath.compile(selector);
                return (Node)expression.evaluate(document, XPathConstants.NODE);
            }
            
            /**
             * Removes all children in a node.
             * @param  document
             * @param  selector
             * @throws XPathExpressionException
             */
            private static void documentEmptyNode(final Document document, final String selector)
                    throws XPathExpressionException {
                final var xpath = XPathFactory.newInstance().newXPath();
                final var expression = xpath.compile(selector);
                final var nodes = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
                for (final var node : Multiplex.convertNodeList(nodes)) {
                    final var childs = node.getChildNodes();
                    for (final var child : Multiplex.convertNodeList(childs))
                        node.removeChild(child);
                }
            }

            /**
             * Removes elements from the document which match the selector.
             * @param  document
             * @param  selector
             * @throws XPathExpressionException
             */
            private static void documentRemoveNode(final Document document, final String selector)
                    throws XPathExpressionException {
                final var xpath = XPathFactory.newInstance().newXPath();
                final var expression = xpath.compile(selector);
                final var nodes = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
                for (var loop = 0; loop < nodes.getLength(); loop++)
                    nodes.item(loop).getParentNode().removeChild(nodes.item(loop));
            }

            /**
             * Adds a borderless style to the document.
             * This style is needed for the header and footer.
             * @param  document
             * @throws XPathExpressionException
             */
            private static void documentBorderless(final Document document)
                    throws XPathExpressionException {

                final var style = document.createElement("style");
                style.setAttribute("type", "text/css");
                style.setTextContent("@page {"
                        + " margin:0mm!important;"
                        + " padding:0mm!important;"
                        + " border:0mm solid!important;}");

                final var xpath = XPathFactory.newInstance().newXPath();
                final var expression = xpath.compile("/html/head");
                final var node = (Node)expression.evaluate(document, XPathConstants.NODE);
                if (Objects.nonNull(node))
                    node.appendChild(style);
            }

            /**
             * Creates an XML string based on the document.
             * @param  document
             * @return an XML string based on the document
             * @throws TransformerException
             */
            private static String documentToString(final  Document document)
                    throws TransformerException {
                final var factory = TransformerFactory.newInstance();
                final var transformer = factory.newTransformer();
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
                transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.US_ASCII.name());
                final var buffer = new ByteArrayOutputStream();
                transformer.transform(new DOMSource(document), new StreamResult(buffer));
                return buffer.toString();
            }

            /**
             * Separates the markup for the fragments: header, content, footer.
             * @param  markup
             * @return a multiplex instance with the extracted markup for
             *     header, content a footer
             * @throws Exception
             */
            public static Multiplex demux(final String markup)
                    throws Exception {

                final var factory = DocumentBuilderFactory.newInstance();
                final var builder = factory.newDocumentBuilder();
                final var document = builder.parse(new ByteArrayInputStream(markup.getBytes()));

                final var multiplex = new Multiplex();

                final var content = Multiplex.documentClone(document);
                Multiplex.documentRemoveNode(content, "/html/body/header");
                Multiplex.documentRemoveNode(content, "/html/body/footer");
                multiplex.content = Multiplex.documentToString(content);

                final var header = Multiplex.documentClone(document);
                var headerNode = Multiplex.documentFetchNode(document, "/html/body/header");
                if (Objects.nonNull(headerNode)) {
                    Multiplex.documentEmptyNode(header, "/html/body");
                    final var headerBody =  Multiplex.documentFetchNode(header, "/html/body");
                    headerNode = header.importNode(headerNode, true);
                    headerBody.appendChild(headerNode);
                    Multiplex.documentBorderless(header);
                    multiplex.header = Multiplex.documentToString(header);
                }

                final var footer = Multiplex.documentClone(document);
                var footerNode = Multiplex.documentFetchNode(document, "/html/body/footer");
                if (Objects.nonNull(footerNode)) {
                    footerNode = footerNode.cloneNode(true);
                    Multiplex.documentEmptyNode(footer, "/html/body");
                    final var footerBody =  Multiplex.documentFetchNode(footer, "/html/body");
                    footerNode = footer.importNode(footerNode, true);
                    footerBody.appendChild(footerNode);
                    Multiplex.documentBorderless(footer);
                    multiplex.footer = Multiplex.documentToString(footer);
                }
                
                return multiplex;
            }
        }
        
        /** TemplateException */
        public static class TemplateException extends ServiceException {

            private static final long serialVersionUID = 2701029837610928746L;

            /** Constructor, creates a new TemplateException */
            public TemplateException() {
                super();
            }

            /**
             * Constructor, creates a new TemplateException
             * @param cause
             */
            public TemplateException(final Throwable cause) {
                super(cause);
            }

            /**
             * Constructor, creates a new TemplateException
             * @param message
             */
            public TemplateException(final String message) {
                super(message);
            }
            
            /**
             * Constructor, creates a new TemplateException
             * @param message
             * @param cause
             */
            public TemplateException(final String message, final Throwable cause) {
                super(message, cause);
            }               
        }
        
        /** Exception when accessing and using template resources. */
        public static class TemplateResourceException extends TemplateException {
            
            private static final long serialVersionUID = -6452881833015318785L;
            
            /** Constructor, creates a new TemplateResourceException. */
            public TemplateResourceException() {
                super();
            }

            /** 
             * Constructor, creates a new TemplateResourceException.
             * @param message
             */
            public TemplateResourceException(final String message) {
                super(message);
            }
        }

        /** Exception if template resources are not found. */
        public static class TemplateResourceNotFoundException extends TemplateResourceException {

            private static final long serialVersionUID = -4532058335049427299L;

            /** Constructor, creates a new TemplateResourceNotFoundException. */
            public TemplateResourceNotFoundException() {
                super();
            }

            /** 
             * Constructor, creates a new TemplateResourceNotFoundException.
             * @param message
             */
            public TemplateResourceNotFoundException(final String message) {
                super(message);
            }
        }
        
        /** Exception for endless recursions. */
        public static class TemplateRecursionException extends TemplateException {

            private static final long serialVersionUID = 6981096067851899978L;
            
            /** Constructor, creates a new TemplateRecursionException. */
            public TemplateRecursionException() {
                super();
            }
        }
    }
    
    /** ServiceException */
    public static class ServiceException extends Exception {

        private static final long serialVersionUID = -6124067136782291330L;

        /** ServiceException */
        ServiceException() {
            super();
        } 

        /**
         * ServiceException
         * @param cause
         */
        ServiceException(final Throwable cause) {
            super(cause);
        } 

        /**
         * ServiceException
         * @param message
         */
        ServiceException(final String message) {
            super(message);
        }
        
        /**
         * ServiceException
         * @param message
         * @param cause
         */
        ServiceException(final String message, final Throwable cause) {
            super(message, cause);
        }        
    }
}