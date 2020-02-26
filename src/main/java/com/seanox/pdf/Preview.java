/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der GNU General Public License.
 *
 * PDF Service
 * Copyright (C) 2020 Seanox Software Solutions
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.seanox.pdf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Tool for the design process to create a test output of the generated PDFs
 * into the working directory.
 * 
 * @version 3.1.0
 */
public class Preview {

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
            File output = new File(template.getSimpleName() + ".pdf");
            output.delete();
            Files.write(output.toPath(), template.newInstance().getPreview(), StandardOpenOption.CREATE);
        } catch (Exception exception) {
            System.out.println("ERROR: " + template + " failed");
            exception.printStackTrace(System.err);
            throw exception;
        }
        System.out.println("INFORMATION: " + template + " done");
    }
    
    /**
     * Main entry for the console application.
     * @param  options not supported
     * @throws Exception
     *     In case of unexpected errors.
     */    
    public static void main(String[] options) {
        
        try {Preview.execute();
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
        }
    }
}