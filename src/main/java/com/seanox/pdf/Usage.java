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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** 
 * Output of information on how to use the command line tools.<br>
 * <br>
 * Usage 3.8.0 20200605<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.8.0 20200605
 */
public class Usage {

    private static String catchToolInfos(Class<?> tool) {
        
        PrintStream output = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream cache = new PrintStream(buffer)) {
            System.setOut(cache);
            try {tool.getMethod("main", String[].class).invoke(null, new Object[] {null});
            } catch (Exception exception) {
                return String.valueOf(exception);
            }
        }
        System.setOut(output);
        String summary = buffer.toString();
        String version = summary.replaceAll("(?s)\\R.*$", "");
        String usage = summary.replaceAll("(?s)^.*(?<=\\Rusage:)\\s*([^\r\n]+)\\s*.*$", "$1");
        
        return version + System.lineSeparator() + usage;
    }
    
    /**
     * Main entry for the console application.
     * @param  options ignored
     * @throws Exception
     *     In case of unexpected errors.
     */     
    public static void main(String[] options) throws Exception {
        
        System.out.println("Seanox PDF Tools Usage [Version 3.8.0 20200605]");
        System.out.println("Copyright (C) 2020 Seanox Software Solutions");
        System.out.println();
        System.out.println("This java archive contains several command line tools.");
        System.out.println();
        
        System.out.println(Usage.catchToolInfos(Compare.class));
        System.out.println();
        System.out.println(Usage.catchToolInfos(Designer.class));
        System.out.println();
        System.out.println(Usage.catchToolInfos(Preview.class));
        System.out.println();
    }
}