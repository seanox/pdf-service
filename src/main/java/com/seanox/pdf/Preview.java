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

import com.openhtmltopdf.util.XRLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Command line tool that creates a PDF preview of all templates with the
 * annotation {@link com.seanox.pdf.Service.Template.Resources} in the
 * ClassPath and of all files found for the specified paths, filters and
 * globs.<br>
 * <br>
 * The preview is based on the mock-up data in the properties files for the
 * templates.<br>
 * <br>
 * Preview 4.2.0 20220806<br>
 * Copyright (C) 2022 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
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
     * Determines the output file for another file.
     * @param  file
     * @return determined output file
     */
    static File locateOutput(final File file) {
        
        var target = file.getAbsolutePath();
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
        
        for (final var template : Service.Template.scan()) {
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
     * Creates test outputs of one PDF to the working directory.
     * @param  template
     * @throws Exception
     *     In case of unexpected errors.
     */
    static void execute(final Class<Service.Template> template)
            throws Exception {
        final var instance = (Template)Service.Template.instantiate(template);
        final var output = Preview.locateOutput(new File(instance.getSource()));
        output.delete();
        Files.write(output.toPath(), instance.getPreview(), StandardOpenOption.CREATE);
    }
    
    /**
     * Creates test outputs of one PDF to the template directory.
     * The creation is based on markup without own template implementation.
     * @param  file
     * @throws Exception
     *     In case of unexpected errors.
     */    
    static void execute(final File file)
            throws Exception {
        
        if (!file.isFile()
                || !file.exists())
            return;

        final var canonical = file.getCanonicalFile();
        final var output = Preview.locateOutput(canonical);
        output.delete();

        final var template = new Template() {
            
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
            protected URI getResource(final String resource)
                    throws Exception {
                final var target = new File(canonical.getParentFile(), resource).getCanonicalFile();
                if (!target.isFile()
                        || !target.exists())
                    throw new Template.TemplateResourceNotFoundException(target.toString());
                return target.toURI();
            }
            
            @Override
            protected InputStream getResourceStream(final String resource)
                    throws Exception {
                final var target = new File(canonical.getParentFile(), resource).getCanonicalFile();
                if (!target.isFile()
                        || !target.exists())
                    throw new Template.TemplateResourceNotFoundException(target.toString());
                return new FileInputStream(target);
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
    public static void main(final String... options)
            throws Exception {
        
        System.out.println("Seanox PDF Preview [Version 4.1.2 20220713]");
        System.out.println("Copyright (C) 2022 Seanox Software Solutions");
        System.out.println();

        if (Objects.isNull(options)
                || Arrays.stream(options).anyMatch(option -> option.matches("(?i)^-h(elp)?$"))) {
            System.out.println("usage: java -cp seanox-pdf-tools.jar com.seanox.pdf.Preview <path> ...");
            System.out.println();
            System.out.println("- Paths support glob patterns and @Resources to scan the classpath.");
            System.out.println("- When using paths without @Resources, the classpath is not scanned.");
            System.out.println("- Without arguments @Resources is used as default to scan the classpath.");
            return;
        }
        
        // First the simple templates, which only use static preview data from
        // the properties. However, these may also be used later by template
        // implementation with dynamic preview data, in which case they are
        // simply overwritten in the next step.

        for (final var option : options) {
            if (("@Resources").equalsIgnoreCase(option))
                continue;
            final var path = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$1");
            final var glob = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$2");
            try (final var stream = Files.newDirectoryStream(
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

        if (options.length <= 0
                || Arrays.stream(options).anyMatch(option -> option.matches("(?i)^@Resources$")))
            Preview.execute();
    }
}