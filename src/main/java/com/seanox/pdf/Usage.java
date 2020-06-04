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

import java.io.InputStream;

import org.apache.pdfbox.io.IOUtils;

/** 
 * Output of information on how to use the command line tools.<br>
 * <br>
 * Usage 1.0.0 20200604<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.0.0 20200604
 */
public class Usage {
    
    /**
     * Main entry for the console application.
     * @param  options ignored
     * @throws Exception
     *     In case of unexpected errors.
     */     
    public static void main(String[] options) throws Exception {
        
        System.out.println("Seanox PDF Tools Usage [Version 1.0.0 20200604]");
        System.out.println("Copyright (C) 2020 Seanox Software Solutions");
        System.out.println();

        String resource = "/" + Usage.class.getName().replace(".", "/") + ".txt";
        try (InputStream input = Usage.class.getResourceAsStream(resource)) {
            String output = new String(IOUtils.toByteArray(input));
            for (String line : output.split("\\R"))
                System.out.println(line);
        }
    }
}