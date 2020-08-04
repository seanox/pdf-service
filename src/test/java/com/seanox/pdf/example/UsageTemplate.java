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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Template;

/** 
 * Example of using the PDF service.
 * Template and preview data are in the same package.
 * The resources (CSS, images, fonts, ...) are in the ClassPath /pdf/... and are
 * used in the template relative.<br>
 * <br>
 * UsageTemplate 1.2.1 20200804<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.2.1 20200804
 */
@SuppressWarnings("javadoc")
public class UsageTemplate {
    
    public static void main(String... options) {
 
        //The static texts are required as a Map<String, String>.
        //Often they are available in a similar way as properties or JSON file
        //and can be easily converted.
        //Static texts are available in the template in the header, dataset and
        //footer sections.
        Map<String, String> statics = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L; {
            put("ARTICLE_NUMBER", "Article Number");
            put("ARTICLE_PRICE", "Price");
            put("ADDRESS_TEL", "Tel");
            put("ADDRESS_FAX", "Fax");
            put("ADDRESS_E_MAIL", "E-Mail");
            put("ADDRESS_WEB", "Web");
        }};
        
        //The template is configured via a meta object.
        Meta meta = new Meta();
        meta.setStatics(statics);
        meta.setData(new HashMap<>());
        
        //The delegate returns a entity.
        //The template generator expects a structured map, that means keys as
        //string and values as collection + string.
        //The ObjectMapper creates this map.        
        meta.getData().put("outlet", new ObjectMapper().convertValue(ExampleOutletDelegate.get(), Map.class));

        //The delegate returns a list of entities.
        //The template generator expects a structured map, that means keys as
        //string and values as collection + string.
        //The ObjectMapper creates this map.        
        meta.getData().put("articles", ExampleArticleDelegate.list().stream().map(
                entity -> new ObjectMapper().convertValue(entity, Map.class)
            ).collect(Collectors.toList()));
        
        
        //The render-method of the template creates the final PDF.
        //The PDF is output to the current working directory.
        //The file name is derived from the UsageTemplate class.
        try {
            byte[] data = Service.render(ExampleTemplate.class, meta);
            Files.write(Paths.get(UsageTemplate.class.getSimpleName() + ".pdf"), data, StandardOpenOption.CREATE);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    //Simulation of the data access layer as delegate.
    private static class ExampleOutletDelegate {
        
        static Object get() {
            return new Object() {
                public String getName() {
                    return "Jane Doe Toys Limited";
                }
                public String getStreet() {
                    return "Western Road";
                }
                public String getLocation() {
                    return "GB BN1 2NW Brighton";
                }
                public String getPhone() {
                    return "+44 1234 05678 0";
                }
                public String getFax() {
                    return "+44 1234 05678 1";
                }
                public String getEmail() {
                    return "mail@outlet.local";
                }
                public String getWebsiteUrl() {
                    return "https://outlet.local";
                }
            };    
        }
    }
    
    //Simulation of the data access layer as delegate.
    private static class ExampleArticleDelegate {
        
        static List<Object> list() {
            return new ArrayList<Object>() {{
                add(new Object() {
                    public String getTitle() {
                        return "Example A-1";
                    }
                    public String getArticleNumber() {
                        return "A-1";
                    }
                    public String getPrice() {
                        return "123.00 GBP";
                    }
                });
                add(new Object() {
                    public String getTitle() {
                        return "Example B-2";
                    }
                    public String getArticleNumber() {
                        return "B-2";
                    }
                    public String getPrice() {
                        return "234.00 GBP";
                    }
                });
                add(new Object() {
                    public String getTitle() {
                        return "Example C-3";
                    }
                    public String getArticleNumber() {
                        return "C-3";
                    }
                    public String getPrice() {
                        return "345.00 GBP";
                    }
                });                
            }};
        }
    }
    
    //The template is derived from Template and therefore requires only a
    //annotation. Optionally, the path from the template can also be specified
    //if the template is not in the template implementation.
    //Base defines where in the ClassPath the resources (CSS, images, fonts, ...)
    //for PDF creation are located.
    @Resources(base="/pdf")
    public static class ExampleTemplate extends Template {
    }
}