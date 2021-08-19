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

import com.openhtmltopdf.util.XRLog;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Deamon for the design process, for the continuous creation of PDFs if the
 * templates change. The compilation is done by the IDE and the daemon are
 * started directly in the IDE. The templates are edited in the source
 * directory. The PDFs are output in the same directory as the template.<br>
 * <br>
 * Designer 4.1.0 20210818<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.1.0 20210818
 */
public class Designer {
    
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
     * Main entry for the console application.
     * @param  options optional list with paths and filters/globs of templates
     * @throws Exception
     *     In case of unexpected errors.
     */
    public static void main(final String... options)
            throws Exception {
        
        System.out.println("Seanox PDF Design Deamon [Version 0.0.0 00000000]");
        System.out.println("Copyright (C) 0000 Seanox Software Solutions");
        System.out.println();

        if (Objects.isNull(options)
                || Arrays.stream(options).anyMatch(option -> option.matches("(?i)^-h(elp)?$"))) {
            System.out.println("usage: java -cp seanox-pdf-tools.jar com.seanox.pdf.Designer <path> ...");
            System.out.println();
            System.out.println("- Paths support glob patterns and @Resources to scan the classpath.");
            System.out.println("- When using paths without @Resources, the classpath is not scanned.");
            System.out.println("- Without arguments @Resources is used as default to scan the classpath.");
            return;
        }

        System.out.println("Press Ctrl+C to stop");
        System.out.println();
        
        Service.Template.scan();
        
        final HashMap<File, Date> fileMap = new HashMap<>();
        while (true) {
            try {Thread.sleep(1000);
            } catch (InterruptedException exception) {
                break;
            }

            if (options.length <= 0
                    || Arrays.stream(options).anyMatch(option -> option.matches("(?i)^@Resources$"))) {
                for (Class<Service.Template> template : Service.Template.scan()) {
                    final File file = new File(template.getDeclaredConstructor().newInstance().getSource());
                    final Date lastModified = new Date(file.lastModified());
                    if (Preview.locateOutput(file).exists()
                            && fileMap.containsKey(file)
                            && fileMap.get(file).equals(lastModified))
                        continue;
                    try {Preview.execute(template);
                    } catch (Exception exception) {
                        continue;
                    }
                    fileMap.put(file, lastModified);
                }
            }

            for (String option : options) {
                if (("@Resources").equalsIgnoreCase(option))
                    continue;
                final String path = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$1");
                final String glob = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$2");
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                        Paths.get(path), glob)) {
                    stream.forEach(file -> {
                        Date lastModified = new Date(file.toFile().lastModified());
                        if (Preview.locateOutput(file.toFile()).exists()
                                && fileMap.containsKey(file.toFile())
                                && fileMap.get(file.toFile()).equals(lastModified))
                            return;
                        try {Preview.execute(file.toFile());
                        } catch (Exception exception) {
                            return;
                        }
                        fileMap.put(file.toFile(), lastModified);
                    });
                }
            }
        }
    }
}