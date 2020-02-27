/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der Apache License.
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
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

import com.openhtmltopdf.util.XRLog;

/**
 * Tool for the design process to create a test output of the generated PDFs
 * into the working directory.
 * 
 * @version 3.2.0
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
        
        for (Class<Service.Template> template : Service.Template.scan())
            Preview.execute(template);
    }

    /**
     * Creates test outputs of one PDFs to the working directory.
     * @param  template
     * @throws Exception
     *     In case of unexpected errors.
     */
    static void execute(Class<Service.Template> template)
            throws Exception {
        
        System.out.println("INFORMATION: " + template.getSimpleName() + " started");
        try {
            Template instance = (Template)template.newInstance();
            File output = Preview.locateOutput(new File(instance.getResource("html").toURI()));
            output.delete();
            Files.write(output.toPath(), instance.getPreview(), StandardOpenOption.CREATE);
        } catch (Exception exception) {
            System.out.println("ERROR: " + template + " failed");
            exception.printStackTrace(System.err);
            throw exception;
        }
        System.out.println("INFORMATION: " + template + " done");
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
        
        System.out.println("INFORMATION: " + file.getName() + " started");
        try {
            
            File output = Preview.locateOutput(file);
            output.delete();
            
            Template template = new Template() {
                
                @Override
                protected URI getBaseURI() throws URISyntaxException {
                    return file.getParentFile().toURI();
                }
                
                @Override
                protected URL getSource() throws Exception {
                    return file.toURI().toURL();
                }
                
                @Override
                protected URL getResource(String extension) throws Exception {
                    
                    String target = file.getAbsolutePath();
                    target = target.replaceAll("\\.[^\\.]*$", "." + extension);
                    if (!target.matches("^.*\\.[^\\.]*$"))
                        target += "." + extension;
                    
                    File resource = new File(target);
                    if (!resource.isFile()
                            || !resource.exists())
                        throw new FileNotFoundException(target);
                    return resource.toURI().toURL();
                }
            };
            
            Files.write(output.toPath(), template.getPreview(), StandardOpenOption.CREATE);
        } catch (Exception exception) {
            System.out.println("ERROR: " + file.getName() + " failed");
            exception.printStackTrace(System.err);
            throw exception;
        }
        System.out.println("INFORMATION: " + file.getName() + " done");
    }
    
    /**
     * Main entry for the console application.
     * @param  options optional list with paths and filters/globs of templates
     * @throws Exception
     *     In case of unexpected errors.
     */    
    public static void main(String[] options) {

        try {
            Preview.execute();
            if (options != null) {
                for (String option : options) {
                    String path = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$1");
                    String glob = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$2");
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                            Paths.get(path), glob)) {
                        stream.forEach(file -> {
                            try {Preview.execute(file.toFile());
                            } catch (Exception exception) {
                                System.out.println("ERROR: " + file.toFile().getName() + " failed");
                                exception.printStackTrace(System.err);
                            }
                        });
                    }                    
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }
}