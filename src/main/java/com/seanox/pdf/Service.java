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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.seanox.pdf.Service.Template.Resources;

/**
 * Static service for creating PDF based on templates and meta-objects.
 *
 * <dir><b>Examples of use:</b></dir>
 * <pre>
 *   Service.generate(template, meta);
 *    
 *   Files.write(new File(template + ".pdf").toPath(), Service.generate(template, meta), StandardOpenOption.CREATE);
 * </pre>
 *  
 * <dir><b>How it works:</b></dir>
 * The creation of PDFs is based on an HTML-to-PDF converter (openhtmltopdf).
 * In the first step, an HTML is created that contains all data records.
 * The HTML is based on a markup template with placeholders. A generator or
 * renderer fills the placeholders in the template and creates an
 * Single-Page-HTML as text.
 * The HTML-to-PDF converter creates the PDF from the HTML.
 * The pages are separated by CSS.

 * The service works data neutral. There is no special data object.
 * Only the key value entries in the maps and the placeholders in the template
 * determine the content.
 *  
 * <dir><b>Useful information:</b></dir>
 * Templates are based on an implementation of the {@link Service.Template} and
 * the annoation {@link Resources}, which with {@link Resources#base()} and
 * {@link Resources#template()}) contains information about the base directory
 * of the resources (stylesheets, images, fonts, ...), as well the path of the
 * markup template with the same name.
 * It is practical if the template implementation, the markup template and any
 * template extensions (properties, ...) are stored in the same package.
 * 
 * The resources (stylesheets, images, fonts, ...) use HTML-to-PDF from the
 * ClassPath, means the base URI required by the HTML-to-PDF converter refers to
 * the ClassPath of this class. The location in the ClassPath can be defined
 * with {@link Resources#base()}.
 *  
 * <dir><b>About the templates</b></dir>
 * The template implementation takes over the rendering of the templates.
 * The implementation decides which generator, renderer, engine, ... it uses.
 * As engine {@link Generator} is used, here you can find more details.
 * The most important in short form:
 *  
 * <dir><code>#[palceholder]</code></dir>
 * Simple placeholder, global or in a section.
 *  
 * <dir><code>#[section[[...]]]</code></dir>
 * Section/Bock can contain more substructures.
 * Sections/blocks are only rendered if a corresponding map entry exists.
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
 * Service 3.4x.0 20200309<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.4x.0 20200309
 */
public class Service {
    
    /**
     * Creates a PDF for a template and data records as meta object.
     * @param  template {@link Template}
     * @param  meta     {@link Meta}
     * @return the created PDF as byte array
     * @throws ServiceException
     *     In case of unexpected errors.
     */
    public static byte[] generate(Template template, Meta meta)
            throws ServiceException {
        
        try {new URI(template.getBaseURI().toString());
        } catch (Exception exception) {
            throw new Template.TemplateException("Invalid base URI", exception);
        }

        if (meta == null)
            meta = new Meta();
        try {return template.generate(meta);
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Meta object for creating PDFs.
     * The PDF creation is based on templates and is decoupled from the business
     * logic. The templates only know placeholders and structures.
     * Templates consist of the parts: header, content and footer.
     * Content is based on a set/collection of data objects that are projected
     * here in the form of key-value-maps, comparable to JSON as a nested data
     * structure. Header and footer have their own key-value-maps.
     * In this meta object, the maps for the different parts of a PDF are
     * summarized/collected.
     */
    public static class Meta {

        /** locale */
        private Locale locale;

        /** key-value map for the header */
        private Map<String, Object> header;

        /** key-value map for the content */
        private Collection<Map<String, Object>> dataset;
        
        /** key-value map for the static texts */
        private Map<String, String> statics;

        /** key-value map for the footer */
        private Map<String, Object> footer;
        
        /** 
         * Enables that for each data record a new PDF begins, which is later
         * combined into one. This option forces the styles to be repeated on
         * the first and last page.
         * Default value: {@code false} (disabled)
         */
        private boolean smart;
        
        /** Constructor, creates a new Meta object. */
        public Meta() {
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
         * Return value of header.
         * @return value of header
         */
        public Map<String, Object> getHeader() {
            return this.header;
        }

        /**
         * Set value of header.
         * @param header value of header
         */
        public void setHeader(Map<String, Object> header) {
            this.header = header;
        }

        /**
         * Return value of dataset.
         * @return value of dataset
         */
        public Collection<Map<String, Object>> getDataset() {
            return this.dataset;
        }

        /**
         * Set value of dataset.
         * @param dataset value of dataset
         */
        public void setDataset(Collection<Map<String, Object>> dataset) {
            this.dataset = dataset;
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

        /**
         * Return value of footer.
         * @return value of footer
         */
        public Map<String, Object> getFooter() {
            return this.footer;
        }

        /**
         * Set value of footer.
         * @param footer value of footer
         */
        public void setFooter(Map<String, Object> footer) {
            this.footer = footer;
        }
        
        /**
         * Return value of smart.
         * @return value of smart
         */
        public boolean isSmart() {
            return this.smart;
        }

        /**
         * Set value of smart.
         * @param smart value of smart
         */
        public void setSmart(boolean smart) {
            this.smart = smart;
        }

        @Override
        protected Object clone() {
            
            Meta meta = new Meta();
            meta.dataset = this.dataset;
            meta.footer  = this.footer;
            meta.header  = this.header;
            meta.locale  = this.locale;
            meta.statics = this.statics;
            meta.smart   = this.smart;
            return meta;
        }

        /**
         * Meta-Type is required during generation so that the corresponding
         * meta data (Key-Value Map) is used for the markup.
         */
        public static enum Type {

            HEADER,

            DATA,

            FOOTER
        }
    }

    /** Abstract class for implementing templates. */
    public static abstract class Template {
        
        /**
         * Templates are based on an implementation of the
         * {@link Service.Template} and the annoation {@link Resources}, which
         * with {@link Resources#base()} and {@link Resources#template()})
         * contains information about the base directory of the resources
         * (stylesheets, images, fonts, ...), as well the path of the markup
         * template with the same name.
         */
        @Documented
        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Resources {

            /** Base URI of the resources in the ClassPath */
            String base() default "/";

            /** 
             * Path of the markup template or only the extension, if the
             * template is in the same package.
             */
            String template() default "html";
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

            if (Template.templates != null)
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
         * Returns the base URI of resources (stylesheets, images, fonts, ...)
         * @return the base URI of resources
         * @throws URISyntaxException
         *     In case of an invalid URI syntax.
         */
        protected URI getBaseURI()
                throws URISyntaxException {
            
            Resources resource = this.getClass().getAnnotation(Resources.class);
            return new URI(resource != null ? resource.base() : "/");
        }
        
        /**
         * Returns the markup template.
         * @return the markup template
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected URL getSource()
                throws Exception {
            
            Resources resource = this.getClass().getAnnotation(Resources.class);
            String template = resource != null ? resource.template() : "html";
            if (template.matches("\\w+"))
                return this.getResource(template);
            return Service.class.getResource(template);
        }

        /**
         * Returns the {@link InputStream} to the markup template.
         * @return the {@link InputStream} to the markup template
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected InputStream getSourceStream()
                throws Exception {
            
            byte[] data = IOUtils.toByteArray(this.getSource().openStream());
            return new ByteArrayInputStream(data);
        }
        
        /**
         * Returns the template extension with the same name in the same package.
         * @param  extension
         * @return the template extension
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected URL getResource(String extension)
                throws Exception {
            
            Resources resource = this.getClass().getAnnotation(Resources.class);
            String template = resource != null ? resource.template() : "html";
            if (!template.matches("\\w+")) {
                String target = template.replaceAll("\\.[^\\.]*$", "." + extension);
                if (!target.matches("^.*\\.[^\\.]*$"))
                    target += "." + extension;
                extension = target;
            } else extension = "/" + this.getClass().getName().replace('.', '/') + "." + extension;
            
            if (Service.class.getResource(extension) == null)
                throw new FileNotFoundException(extension);
            return Service.class.getResource(extension);
        }

        /**
         * Returns the {@link InputStream} to template extension.
         * @param  extension
         * @return the {@link InputStream} to the template extension
         * @throws Exception
         *     In case of unexpected errors.
         */
        protected InputStream getResourceStream(String extension)
                throws Exception {
            
            byte[] data = IOUtils.toByteArray(this.getResource(extension).openStream());
            return new ByteArrayInputStream(data);
        }
        
        /**
         * Returns the markup of the template.
         * @return the markup of the template
         * @throws Exception
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
         * Creates the PDF as preview.
         * @return the PDF as preview
         * @throws Exception
         *     In case of unexpected errors.
         */
        @SuppressWarnings("unchecked")
        protected byte[] getPreview()
                throws Exception {
            return generate(new Meta() {{
                this.setLocale(Locale.getDefault());
                
                this.setHeader(Template.this.getPreviewData().entrySet().stream()
                        .filter(e -> !e.getKey().equalsIgnoreCase("static")
                                && !(e.getValue() instanceof Collection))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                
                this.setDataset(new ArrayList<Map<String, Object>>() {
                    private static final long serialVersionUID = 1L; {
                        this.add(Template.this.getPreviewData().entrySet().stream()
                                .filter(e -> !e.getKey().equalsIgnoreCase("static"))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }});
                
                this.setFooter(Template.this.getPreviewData().entrySet().stream()
                        .filter(e -> !e.getKey().equalsIgnoreCase("static")
                                && !(e.getValue() instanceof Collection))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                
                this.setStatics(new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L; {
                        if (Template.this.getPreviewData().containsKey("static"))
                            this.putAll((HashMap<String, String>)Template.this.getPreviewData().get("static"));
                }});
             }});
        }

        /**
         * Creates HTML markup for the PDF based of data in a meta object.
         * @param  markup
         * @param  type
         * @param  meta
         * @return the created HTML markup for the PDF creation
         */
        protected abstract String generate(String markup, Meta.Type type, Meta meta);
        
        /**
         * Merges a collection of PDDocuments into one. 
         * @param  documents
         * @return the merged PDDocuments
         * @throws IOException
         */
        private static PDDocument merge(Collection<PDDocument> documents)
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
         * Creates the PDF based on the data records as meta object.
         * @param  meta data records as map array
         * @return the created PDF as byte array
         * @throws Exception
         *     In case of unexpected errors.
         */
         final protected byte[] generate(Meta meta)
                throws Exception {

            if (meta == null)
                meta = new Meta();

            if (meta.dataset == null)
                meta.dataset = new ArrayList<>();

            //Header and footer are copied on the first level because both are
            //manipulated for the additional values 'page' and 'pages'.
            if (meta.header == null)
                meta.header = new HashMap<>();
            else meta.header = new HashMap<>(meta.header);
            if (meta.footer == null)
                meta.footer = new HashMap<>();
            else meta.footer = new HashMap<>(meta.footer);

            URI base = getBaseURI();
            if (base.getScheme() == null) {
                if (Service.class.getResource(base.toString()) == null)
                    throw new FileNotFoundException(base.toString());
                base = Service.class.getResource(base.toString()).toURI();
            }
            if (!base.toString().endsWith("/"))
                base = new URI(base.toString() + "/");

            String markup = this.getMarkup();

            Pattern pattern = Pattern.compile("(?i)(?:^|(?<=[\r\n]))\\s*#include(?:(?:\\s+([^\r\n]*)\\s*((?=[\r\n])|$))|(?=\\s*$))");
            Matcher matcher = pattern.matcher(markup);
            while (matcher.find()) {
                if (matcher.groupCount() < 1)
                    throw new TemplateException("Invalid include found");
                String patch = matcher.group(1);
                if (!patch.startsWith("/")) {
                    if (base != null
                            && StringUtils.isNotEmpty(base.toString())) {
                        patch = base.toString();
                        if (!patch.endsWith("/"))
                            patch += "/";
                        patch += matcher.group(1);
                    } else patch = "/" + patch;
                }
                if (Service.class.getResource(patch) == null) {
                    if (patch.toLowerCase().startsWith("file:"))
                        patch = new File(new URI(patch)).toString();
                    if (!new File(patch).exists())
                        throw new FileNotFoundException(patch);
                    patch = new String(Files.readAllBytes(new File(patch).toPath()));
                } else patch = new String(IOUtils.toByteArray(Service.class.getResourceAsStream(patch)));
                markup = markup.replace(matcher.group(0), patch);
            }
            
            Multiplex multiplex = Multiplex.demux(markup);

            List<PDDocument> artifacts = new ArrayList<>();

            if (meta.dataset != null
                    && !meta.dataset.isEmpty()
                    && meta.isSmart()) {
                Meta temp = (Meta)meta.clone();
                for (Map<String, Object> data : meta.dataset) {
                    temp.dataset = new ArrayList<>();
                    temp.dataset.add(data);
                    
                    ByteArrayOutputStream content = new ByteArrayOutputStream();
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    builder.withHtmlContent(this.generate(multiplex.content, Meta.Type.DATA, temp), base.toString());
                    builder.toStream(content);
                    builder.run();
                    
                    artifacts.add(PDDocument.load(content.toByteArray()));
                }
            } else {
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(this.generate(multiplex.content, Meta.Type.DATA, meta), base.toString());
                builder.toStream(content);
                builder.run();
                artifacts.add(PDDocument.load(content.toByteArray()));
            }

            try (PDDocument document = Service.Template.merge(artifacts)) {

                Splitter splitter = new Splitter();
                List<PDDocument> pages = splitter.split(document);
                meta.header.put("pages", String.valueOf(pages.size()));
                meta.footer.put("pages", String.valueOf(pages.size()));
                
                for (PDDocument page : new ArrayList<>(pages)) {
                    
                    int offset = pages.indexOf(page);
                    
                    PdfRendererBuilder builder;
                    
                    meta.header.put("page", String.valueOf(offset +1));
                    ByteArrayOutputStream header = new ByteArrayOutputStream();
                    builder = new PdfRendererBuilder();
                    builder.withHtmlContent(this.generate(multiplex.header, Meta.Type.HEADER, meta), base.toString());
                    builder.toStream(header);
                    builder.run();
                    
                    try (Overlay overlay = new Overlay()) {
                        overlay.setInputPDF(page);
                        overlay.setAllPagesOverlayPDF(PDDocument.load(header.toByteArray()));
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        overlay.overlay(new HashMap<>()).save(output);
                        page = PDDocument.load(output.toByteArray());
                    }
                    
                    meta.footer.put("page", String.valueOf(offset +1));
                    ByteArrayOutputStream footer = new ByteArrayOutputStream();
                    builder = new PdfRendererBuilder();
                    builder.withHtmlContent(this.generate(multiplex.footer, Meta.Type.FOOTER, meta), base.toString());
                    builder.toStream(footer);
                    builder.run();  
                    
                    try (Overlay overlay = new Overlay()) {
                        overlay.setInputPDF(page);
                        overlay.setAllPagesOverlayPDF(PDDocument.load(footer.toByteArray()));
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        overlay.overlay(new HashMap<>()).save(output);
                        page = PDDocument.load(output.toByteArray());
                    }
                    
                    pages.add(offset +1, page);
                    pages.remove(offset);
                }
                
                try (PDDocument release = Service.Template.merge(pages)) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    release.save(output);
                    return output.toByteArray();
                }
            } 
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
         * content, footer. So that three templates can be created from one
         * template and each fragment can be rendered individually as a PDF.
         */
        private static class Multiplex {

            /** markup of header */
            private String header;

            /** markup of content */
            private String content;

            /** markup of footer */
            private String footer;

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
                if (node != null)
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
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                transformer.transform(new DOMSource(document), new StreamResult(buffer));

                return buffer.toString();
            }

            /**
             * Separates the markup for the fragments: header, content, footer.
             * @param  markup
             * @return a multiplex instance with the extracted markup for
             *     header, content an footer
             * @throws Exception
             */
            private static Multiplex demux(String markup)
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
                Multiplex.documentRemoveNode(header, "/html/body/*[name() != 'header']");
                Multiplex.documentBorderless(header);
                multiplex.header = Multiplex.documentToString(header);
                
                Document footer = Multiplex.documentClone(document);
                Multiplex.documentRemoveNode(footer, "/html/body/*[name() != 'footer']");
                Multiplex.documentBorderless(footer);
                multiplex.footer = Multiplex.documentToString(footer);
                
                return multiplex;
            }
        }
        
        /** TemplateException */
        public static class TemplateException extends ServiceException {

            private static final long serialVersionUID = 2701029837610928746L;

            /**
             * TemplateException
             * @param message
             */
            public TemplateException(String message) {
                super(message);
            }
            
            /**
             * TemplateException
             * @param message
             * @param cause
             */
            public TemplateException(String message, Throwable cause) {
                super(message, cause);
            }               
        }
    }
    
    /** ServiceException */
    public static class ServiceException extends Exception {

        private static final long serialVersionUID = -6124067136782291330L;

        /**
         * ServiceException
         * @param message
         */
        public ServiceException(String message) {
            super(message);
        }
        
        /**
         * ServiceException
         * @param cause
         */
        public ServiceException(Throwable cause) {
            super(cause);
        }        
        
        /**
         * ServiceException
         * @param message
         * @param cause
         */
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }        
    }
}