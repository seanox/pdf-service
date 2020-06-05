/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt,
 * im Folgenden Seanox Software Solutions oder kurz Seanox genannt.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.logging.Level;

import com.openhtmltopdf.util.XRLog;

/**
 * Tool for the design process to create a test output of the rendered PDFs.
 * The PDFs are output in the same directory as the template.<br>
 * <br>
 * Preview 3.3.2 20200601<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.3.1 20200601
 */
public class Preview {
    
    static {
        XRLog.setLevel("com.openhtmltopdf.config", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.exception", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.general", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.init", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.junit", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.load", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.match", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.cascade", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.load.xml-entities", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.css-parse", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.layout", Level.WARNING);
        XRLog.setLevel("com.openhtmltopdf.render", Level.WARNING);
    }

    /**
     *  Determines the output file for another file.
     *  @param  file
     *  @return determined output file
     */
    static File locateOutput(File file) {
        
        String target = file.getAbsolutePath();
        target = target.replaceAll("\\.[^\\.]*$", ".pdf");
        if (!target.matches("^.*\\.[^\\.]*$"))
            target += ".pdf";
        return new File(target);
    }

    /**
     * Creates test outputs of the PDFs to the working directory.
     * @throws Exception
     *     In case of unexpected errors.
     */
    static void execute()
            throws Exception {
        
        for (Class<Service.Template> template : Service.Template.scan()) {
            System.out.println("INFORMATION: " + template.getSimpleName() + " started");
            try {
                Preview.execute(template);
                System.out.println("INFORMATION: " + template + " done");
            } catch (Exception exception) {
                System.out.println("ERROR: " + template + " failed");
                exception.printStackTrace(System.err);
            }
        }
    }

    /**
     * Creates test outputs of one PDFs to the working directory.
     * @param  template
     * @throws Exception
     *     In case of unexpected errors.
     */
    static void execute(Class<Service.Template> template)
            throws Exception {

        Template instance = (Template)template.getDeclaredConstructor().newInstance();
        File output = Preview.locateOutput(new File(instance.getSource()));
        output.delete();
        Files.write(output.toPath(), instance.getPreview(), StandardOpenOption.CREATE);
    }
    
    /**
     * Creates test outputs of one PDFs to the template directory.
     * The creation is based on markup without own template implementation.
     * @param  file
     * @throws Exception
     *     In case of unexpected errors.
     */    
    static void execute(File file)
            throws Exception {
        
        if (!file.isFile()
                || !file.exists())
            return;
        
        File canonical = file.getCanonicalFile();
        
        File output = Preview.locateOutput(canonical);
        output.delete();
        
        Template template = new Template() {
            
            @Override
            protected URI getBase() {
                return canonical.getParentFile().toURI();
            }

            @Override
            protected String getSourcePath() {
                return "/" + canonical.getName();
            }
            
            @Override
            protected URI getSource()
                    throws Exception {
                return new URI(this.getSourcePath());
            }
            
            @Override
            protected InputStream getSourceStream()
                    throws Exception {
                return this.getResourceStream(this.getSourcePath());
            }
            
            @Override
            protected URI getResource(String resource)
                    throws Exception {
                
                File target = new File(canonical.getParentFile(), resource).getCanonicalFile(); 
                if (!target.isFile()
                        || !target.exists())
                    throw new Template.TemplateResourceNotFoundException(target.toString());
                return target.toURI();
            }
            
            @Override
            protected InputStream getResourceStream(String resource)
                    throws Exception {

                File target = new File(canonical.getParentFile(), resource).getCanonicalFile(); 
                if (!target.isFile()
                        || !target.exists())
                    throw new Template.TemplateResourceNotFoundException(target.toString());
                return new FileInputStream(target);
            }
            
            @Override
            protected Properties getPreviewProperties()
                    throws Exception {
                
                String resource = this.getSourcePath();
                resource = resource.replaceAll("[^\\\\/\\.]+$", "") + "properties";
                
                Properties properties = new Properties();
                properties.load(this.getResourceStream(resource));        
                
                return properties;
            }

            @Override
            public String toString() {
                return canonical.toString();
            }
        };
        
        Files.write(output.toPath(), template.getPreview(), StandardOpenOption.CREATE);
    }
    
    /**
     * Main entry for the console application.
     * @param  options optional list with paths and filters/globs of templates
     *     Without, the current working directory is used.
     * @throws Exception
     *     In case of unexpected errors.
     */    
    public static void main(String[] options)
            throws Exception {
        
        System.out.println("Seanox PDF Preview [Version 3.3.1 20200601]");
        System.out.println("Copyright (C) 2020 Seanox Software Solutions");
        System.out.println();
        
        if (options == null
                || options.length <= 0) {
            System.out.println("usage: java -cp seanox-pdf-tools.jar com.seanox.pdf.Preview <paths/filters/globs> ...");
            return;
        }
        
        //First the simple templates, which only use static preview data from
        //the properties. However, these may also be used later by template
        //implementation with dynamic preview data, in which case they are
        //simply overwritten in the next step.

        if (options != null) {
            for (String option : options) {
                String path = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$1");
                String glob = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$2");
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                        Paths.get(path), glob)) {
                    stream.forEach(file -> {
                        try {
                            file = file.toFile().getCanonicalFile().toPath();
                            System.out.println("INFORMATION: " + file.toFile().getName() + " started");
                            Preview.execute(file.toFile());
                            System.out.println("INFORMATION: " + file.toFile().getName() + " done");
                        } catch (Exception exception) {
                            System.out.println("ERROR: " + file.toFile().getName() + " failed");
                            exception.printStackTrace(System.err);
                        }
                    });
                }                    
            }
        }

        Preview.execute();
    }
}