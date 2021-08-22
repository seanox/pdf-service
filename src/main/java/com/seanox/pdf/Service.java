/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
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
package com.seanox.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Service.Template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Overlay;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
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
import java.lang.reflect.Constructor;
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
 * <dir><b>Examples of use:</b></dir>
 * <pre>
 *   Service.render(template, meta);
 *    
 *   Files.write(Paths.get(template + ".pdf"), Service.render(template, meta),
 *         StandardOpenOption.CREATE);
 * </pre>
 *  
 * <dir><b>How it works:</b></dir>
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
 * <dir><b>Useful information:</b></dir>
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
 * <dir><b>About the templates</b></dir>
 * The template implementation takes over the rendering of the templates.
 * The implementation decides which generator, renderer, engine, ... it uses.
 * As engine {@link Generator} is used, here you can find more details.
 * The most important in short form:
 *  
 * <dir><code>#[placeholder]</code></dir>
 * Simple placeholder, global or in a section.
 *  
 * <dir><code>#[placeholder-exists]</code></dir>
 * Pendant to any placeholder, if the value is not {@code null}, not empty and
 * not blank. Then the placeholder contains the value {@code exists}.
 *  
 * <dir><code>#[section[[...]]]</code></dir>
 * Section/Bock can contain more substructures.
 * Sections/blocks are only rendered if a corresponding map entry exists.
 *  
 * <dir><code>![static-text]</code></dir>
 * Placeholder for static non-structured text e.g. from the ResourceBundle.
 * 
 * <dir><code>![static-text-exists]</code></dir>
 * Pendant to any placeholder of static non-structured text, if the value is not
 * {@code null}, not empty and not blank. Then the placeholder contains the
 * value {@code exists}.
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
 * Available in sections: header, footer<br>
 * <br>
 * Service 4.1.0 20210821<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.1.0 20210821
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
    public static byte[] render(Class<? extends Template> template, Meta meta)
            throws ServiceException {
        
        Template instance;
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
    public static byte[] render(Template template, Meta meta)
            throws ServiceException {
        
        try {new URI(template.getBase().toString());
        } catch (Exception exception) {
            throw new Template.TemplateException("Invalid base URI", exception);
        }

        if (Objects.isNull(meta))
            meta = new Meta();
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
        public Meta(Locale locale) {
            this.locale = locale;
        }
        
        /** 
         * Constructor, creates a new Meta object.
         * @param data
         */
        public Meta(Map<String, Object> data) {
            this.data = data;
        }

        /** 
         * Constructor, creates a new Meta object.
         * @param data
         * @param statics
         */
        public Meta(Map<String, Object> data, Map<String, String> statics) {
            this.data = data;
            this.statics = statics;
        }
        
        /** 
         * Constructor, creates a new Meta object.
         * @param locale
         * @param data
         */
        public Meta(Locale locale, Map<String, Object> data) {
            this.locale = locale;
            this.data = data;
        }

        /** 
         * Constructor, creates a new Meta object.
         * @param locale
         * @param data
         * @param statics
         */
        public Meta(Locale locale, Map<String, Object> data, Map<String, String> statics) {
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
        public void setLocale(Locale locale) {
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
        public void setData(Map<String, Object> data) {
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
        public void setStatics(Map<String, String> statics) {
            this.statics = statics;
        }

        @Override
        protected Object clone() {
            
            Meta meta = new Meta();
            meta.locale = this.locale;
            meta.data = this.data;
            meta.statics = this.statics;
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
            final Constructor constructor = template.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Template)constructor.newInstance();
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
            
            List<Class<Template>> templates = new ArrayList<>();
            for (String basePackage : Stream.of(new Throwable().getStackTrace())
                    .map(element -> element.getClassName().replaceAll("\\W.*$", ""))
                    .distinct()
                    .toArray(String[]::new)) {
                ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
                provider.addIncludeFilter(new AnnotationTypeFilter(Resources.class));
                for (BeanDefinition beanDefinition : provider.findCandidateComponents(basePackage)) {
                    Class<?> template = Class.forName(beanDefinition.getBeanClassName());
                    if (Template.class.isAssignableFrom(template))
                        templates.add((Class<Template>)template);
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
            
            Resources resource = this.getClass().getAnnotation(Resources.class);
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
            
            String template = "/" + this.getClass().getName().replace('.', '/') + ".html";    
            Resources resource = this.getClass().getAnnotation(Resources.class);
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
        protected URI getResource(String resource)
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
        protected InputStream getResourceStream(String resource)
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
            return new String(IOUtils.toByteArray(this.getSourceStream()));
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
        protected abstract String generate(String markup, Type type, Meta meta);
        
        /**
         * Merges a collection of PDDocuments into one. 
         * @param  documents
         * @return the merged PDDocuments
         * @throws IOException
         */
        protected static PDDocument merge(Collection<PDDocument> documents)
                throws IOException {

            PDFMergerUtility merge = new PDFMergerUtility();
            for (PDDocument document : documents) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                document.save(output);
                merge.addSource(new ByteArrayInputStream(output.toByteArray()));
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            merge.setDestinationStream(output);
            merge.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            
            return PDDocument.load(output.toByteArray());
        }
        
        /**
         * Normalizes a path.
         * Relative directives like . and .. are balanced out.
         * If necessary, backslashes are uniformly converted to slashes.
         * The return value contains at least one slash.
         * @param  path
         * @return the normalizes a path
         */
        protected static String normalizePath(String path) {
            
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

            // Data is copied because it is manipulated later for the header and
            // footer by adding the keys and values for locale, page and pages.
            if (Objects.isNull(meta.data))
                meta.data = new HashMap<>();
            else meta.data = new HashMap<>(meta.data);

            URI base = this.getBase();
            if (Objects.isNull(base.getScheme())) {
                if (Objects.isNull(Service.class.getResource(base.toString())))
                    throw new TemplateResourceNotFoundException(base.toString());
                base = Service.class.getResource(base.toString()).toURI();
            }
            if (!base.toString().endsWith("/"))
                base = new URI(base + "/");

            String markup = this.getMarkup();
            Multiplex multiplex = Multiplex.demux(markup);

            List<PDDocument> artifacts = new ArrayList<>();
            
            try {

                PdfRendererBuilder builder;
                
                ByteArrayOutputStream content = new ByteArrayOutputStream();
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
                    
                try (PDDocument document = Template.merge(artifacts)) {

                    Splitter splitter = new Splitter();
                    List<PDDocument> pages = splitter.split(document);
                    meta.data.put("pages", String.valueOf(pages.size()));
                    
                    for (PDDocument page : new ArrayList<>(pages)) {
                        
                        int offset = pages.indexOf(page);
                        
                        meta.data.put("page", String.valueOf(offset +1));
                        
                        if (Objects.nonNull(multiplex.header)
                                && !multiplex.header.trim().isEmpty()) {
                            ByteArrayOutputStream header = new ByteArrayOutputStream();
                            builder = new PdfRendererBuilder();
                            builder.withHtmlContent(this.generate(multiplex.header, Type.HEADER, meta), base.toString());
                            builder.toStream(header);
                            builder.run();
                            
                            try (Overlay overlay = new Overlay()) {
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
                            ByteArrayOutputStream footer = new ByteArrayOutputStream();
                            builder = new PdfRendererBuilder();
                            builder.withHtmlContent(this.generate(multiplex.footer, Type.FOOTER, meta), base.toString());
                            builder.toStream(footer);
                            builder.run();  
                            
                            try (Overlay overlay = new Overlay()) {
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
                    
                    try (PDDocument release = Template.merge(pages)) {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        release.save(output);
                        return output.toByteArray();
                    } finally {
                        for (PDDocument page : pages)
                            page.close();
                    }
                }

            } finally {
                for (PDDocument artifact : artifacts)
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
         * <dir><b>Header</b></dir>
         * The complete (X)HTML document with {@code BODY > HEADER} only.
         * 
         * <dir><b>Content</b></dir>
         * The complete (X)HTMl document without {@code BODY > HEADER} and
         * without {@code BODY > FOOTER}.
         * 
         * <dir><b>Footer</b></dir>
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
            private static List<Node> convertNodeList(NodeList nodes) {
                
                if (Objects.isNull(nodes == null)
                        || nodes.getLength() == 0)
                    return Collections.emptyList();
                
                List<Node> list = new ArrayList<>();
                for (int loop = 0; loop < nodes.getLength(); loop++)
                    list.add(nodes.item(loop));
                return list;
            } 

            /**
             * Creates a copy of the passed document.
             * @param  document
             * @return a copy of the passed document
             * @throws ParserConfigurationException
             */
            private static Document documentClone(Document document)
                    throws ParserConfigurationException {

                Node root = document.getDocumentElement();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document clone = builder.newDocument();
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
            private static Node documentFetchNode(Document document, String selector)
                    throws XPathExpressionException {

                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expression = xpath.compile(selector);
                return (Node)expression.evaluate(document, XPathConstants.NODE);
            }
            
            /**
             * Removes all children in a node.
             * @param  document
             * @param  selector
             * @throws XPathExpressionException
             */
            private static void documentEmptyNode(Document document, String selector)
                    throws XPathExpressionException {

                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expression = xpath.compile(selector);
                NodeList nodes = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
                for (Node node : Multiplex.convertNodeList(nodes)) {
                    NodeList childs = node.getChildNodes();
                    for (Node child : Multiplex.convertNodeList(childs))
                        node.removeChild(child);
                }
            }

            /**
             * Removes elements from the document which match the selector.
             * @param  document
             * @param  selector
             * @throws XPathExpressionException
             */
            private static void documentRemoveNode(Document document, String selector)
                    throws XPathExpressionException {

                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expression = xpath.compile(selector);
                NodeList nodes = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
                for (int loop = 0; loop < nodes.getLength(); loop++)
                    nodes.item(loop).getParentNode().removeChild(nodes.item(loop));
            }

            /**
             * Adds a borderless style to the document.
             * This style is needed for the header and footer.
             * @param  document
             * @throws XPathExpressionException
             */
            private static void documentBorderless(Document document)
                    throws XPathExpressionException {

                Element style = document.createElement("style");
                style.setAttribute("type", "text/css");
                style.setTextContent("@page {"
                        + " margin:0mm!important;"
                        + " padding:0mm!important;"
                        + " border:0mm solid!important;}");

                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression expression = xpath.compile("/html/head");
                Node node = (Node)expression.evaluate(document, XPathConstants.NODE);
                if (Objects.nonNull(node))
                    node.appendChild(style);
            }

            /**
             * Creates an XML string based on the document.
             * @param  document
             * @return an XML string based on the document
             * @throws TransformerException
             */
            private static String documentToString(Document document)
                    throws TransformerException {

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
                transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.US_ASCII.name());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
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
            public static Multiplex demux(String markup)
                    throws Exception {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new ByteArrayInputStream(markup.getBytes()));
                
                Multiplex multiplex = new Multiplex();
                
                Document content = Multiplex.documentClone(document);
                Multiplex.documentRemoveNode(content, "/html/body/header");
                Multiplex.documentRemoveNode(content, "/html/body/footer");
                multiplex.content = Multiplex.documentToString(content);

                Document header = Multiplex.documentClone(document);
                Node headerNode = Multiplex.documentFetchNode(document, "/html/body/header");
                if (Objects.nonNull(headerNode)) {
                    Multiplex.documentEmptyNode(header, "/html/body");
                    Node headerBody =  Multiplex.documentFetchNode(header, "/html/body");
                    headerNode = header.importNode(headerNode, true);
                    headerBody.appendChild(headerNode);
                    Multiplex.documentBorderless(header);
                    multiplex.header = Multiplex.documentToString(header);
                }
                
                Document footer = Multiplex.documentClone(document);
                Node footerNode = Multiplex.documentFetchNode(document, "/html/body/footer");
                if (Objects.nonNull(footerNode)) {
                    footerNode = footerNode.cloneNode(true);
                    Multiplex.documentEmptyNode(footer, "/html/body");
                    Node footerBody =  Multiplex.documentFetchNode(footer, "/html/body");
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
            public TemplateException(Throwable cause) {
                super(cause);
            }

            /**
             * Constructor, creates a new TemplateException
             * @param message
             */
            public TemplateException(String message) {
                super(message);
            }
            
            /**
             * Constructor, creates a new TemplateException
             * @param message
             * @param cause
             */
            public TemplateException(String message, Throwable cause) {
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
            public TemplateResourceException(String message) {
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
            public TemplateResourceNotFoundException(String message) {
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
        ServiceException(Throwable cause) {
            super(cause);
        } 

        /**
         * ServiceException
         * @param message
         */
        ServiceException(String message) {
            super(message);
        }
        
        /**
         * ServiceException
         * @param message
         * @param cause
         */
        ServiceException(String message, Throwable cause) {
            super(message, cause);
        }        
    }
}