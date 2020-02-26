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
import java.util.Date;
import java.util.HashMap;

/**
 * Deamon for the design process, for the continuous creation of PDFs if the
 * templates change. The compilation is done by the IDE and the daemon is
 * started directly in the IDE. The templates are edited in the source
 * directory. The output of the PDFs takes place in the working directory of
 * the project.
 * 
 * @version 3.1.0
 */
public class Designer {
    
    /**
     * Main entry for the console application.
     * @param  options not supported
     * @throws Exception
     *     In case of unexpected errors.
     */
    public static void main(String[] options)
            throws Exception {
        
        HashMap<File, Date> fileMap = new HashMap<>(); 
        while (true) {
            try {Thread.sleep(1000);
            } catch (InterruptedException exception) {
                break;
            }
            for (Class<Service.Template> template : Service.Template.scan()) {
                File file = new File(template.newInstance().getSource().toURI());
                Date lastModified = new Date(file.lastModified());
                if (new File(template.getSimpleName() + ".pdf").exists()
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
    }
}