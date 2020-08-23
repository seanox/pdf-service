/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
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
package com.seanox.pdf.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.util.XRLog;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Template;
import com.seanox.pdf.example.data.ArticleDelegate;
import com.seanox.pdf.example.data.OutletDelegate;
import com.seanox.pdf.example.data.ReportDelegate;

@SuppressWarnings("javadoc")
public class UsageComplexTemplate {
    
    private static final String RESOURCES = "/com/seanox/pdf/example"; 

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
    
    //The template is derived from Template and therefore requires only a
    //annotation. Optionally, the path from the template can also be specified
    //if the template is not in the template implementation.
    //Base defines where in the ClassPath the resources (CSS, images, fonts, ...)
    //for PDF creation are located.
    @Resources(base="/pdf", template=RESOURCES + "/UsageComplexTemplate.html")
    public static class ExampleBTemplate extends Template {
    } 
    
    public static void main(String... options)
            throws Exception {
        
        //The template is configured via a meta object.
        Meta meta = new Meta();

        //The static texts are required as a Map<String, String>.
        //Often they are available in a similar way as properties or JSON file
        //and can be easily converted.
        //Static texts are available in the template in the header, dataset and
        //footer sections.
        Map<String, String> statics = new HashMap<>();
        Properties properties = new Properties();
        properties.load(UsageComplexTemplate.class.getResourceAsStream(RESOURCES + "/messages.properties"));
        for (final String key: properties.stringPropertyNames())
            statics.put(key, properties.getProperty(key));
        meta.setStatics(statics);

        meta.setData(new HashMap<>());
        
        //The delegate returns a entity.
        //The template generator expects a structured map, that means keys as
        //string and values as collection + string.
        //The ObjectMapper creates this map.        
        meta.getData().put("outlet", new ObjectMapper().convertValue(OutletDelegate.get(), Map.class));

        //The delegate returns a list of entities.
        //The template generator expects a structured map, that means keys as
        //string and values as collection + string.
        //The ObjectMapper creates this map.        
        meta.getData().put("articles", ArticleDelegate.list().stream().map(
                entity -> new ObjectMapper().convertValue(entity, Map.class)
            ).collect(Collectors.toList()));
        
        meta.getData().put("report", new ObjectMapper().convertValue(ReportDelegate.get(), Map.class));
        
        //The render-method of the template creates the final PDF.
        //The PDF is output to the current working directory.
        //The file name is derived from the UsageTemplate class.
        try {
            byte[] data = Service.render(ExampleBTemplate.class, meta);
            Files.write(Paths.get(UsageComplexTemplate.class.getSimpleName() + ".pdf"), data, StandardOpenOption.CREATE);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        
        System.out.println(UsageComplexTemplate.class.getName() + " done");
    }
}