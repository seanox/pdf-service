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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import com.openhtmltopdf.util.XRLog;

/**
 * Deamon for the design process, for the continuous creation of PDFs if the
 * templates change. The compilation is done by the IDE and the daemon is
 * started directly in the IDE. The templates are edited in the source
 * directory. The output of the PDFs takes place in the working directory of
 * the project.<br>
 * <br>
 * Designer 3.2.0 20200229<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.2.0 20200229
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
    public static void main(String[] options)
            throws Exception {
        
        Service.Template.scan();
        
        HashMap<File, Date> fileMap = new HashMap<>(); 
        while (true) {
            try {Thread.sleep(1000);
            } catch (InterruptedException exception) {
                break;
            }
            
            for (Class<Service.Template> template : Service.Template.scan()) {
                File file = new File(template.newInstance().getSource().toURI());
                Date lastModified = new Date(file.lastModified());
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
            
            if (options != null) {
                for (String option : options) {
                    String path = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$1");
                    String glob = option.replaceAll("^(?:(.*)[/\\\\])*(.*)$", "$2");
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
}