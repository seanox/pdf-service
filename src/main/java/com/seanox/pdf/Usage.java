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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** 
 * Output of information on how to use the command line tools.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
public class Usage {

    private static String catchToolInfos(final Class<?> tool) {
        
        final var output = System.out;
        final var buffer = new ByteArrayOutputStream();
        try (final var cache = new PrintStream(buffer)) {
            System.setOut(cache);
            try {tool.getMethod("main", String[].class).invoke(null, new Object[]{null});
            } catch (Exception exception) {
                return String.valueOf(exception);
            }
        }
        System.setOut(output);
        final var summary = buffer.toString();
        final var version = summary.replaceAll("(?s)\\R.*$", "");
        final var usage = summary.replaceAll("(?s)(^.*\\R(?=usage:))|(\\s*$)", "").replaceFirst("\\R", "");

        return version + System.lineSeparator() + usage;
    }
    
    /**
     * Main entry for the console application.
     * @param options ignored
     */
    public static void main(final String... options) {
        
        System.out.println("Seanox PDF Tools Usage [Version 0.0.0 00000000]");
        System.out.println("Copyright (C) 0000 Seanox Software Solutions");
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