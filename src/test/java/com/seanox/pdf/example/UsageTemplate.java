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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seanox.pdf.Service;
import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Template;

/** 
 * Example of using the PDF service.
 * Template and preview data are in the same package.
 * The resources (css, images, ...) are in the ClassPath /pdf/... and are used
 * in the template relative.<br>
 * <br>
 * UsageTemplate 1.2.0 20200417<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.2.0 20200417
 */
public class UsageTemplate {
    
    public static void main(String[] options) {
 
        //The static texts are required as a Map<String, String>.
        //Often they are available in a similar way as properties or JSON file
        //and can be easily converted.
        //Static texts are available in the template in the header, dataset and
        //footer sections.
        Map<String, CharSequence> statics = new HashMap<String, CharSequence>() {{
            put("ARTICLE_NUMBER", "Article number");
            put("ARTICLE_PRICE", "Price");
            put("ADDRESS_TEL", "Tel");
            put("ADDRESS_FAX", "Fax");
            put("ADDRESS_E_MAIL", "E-Mail");
            put("ADDRESS_WEB", "Web");
        }};
        
        //The data is fetched from the data access layer.
        //In this case a delegate is simulated.
        List<ExampleEntity> dataset = ExampleDelegate.list();

        //The template is configured via a meta object.
        Meta meta = new Meta();
        meta.setStatics(statics);

        //The delegate returns a list of entities.
        //The template generator expects a structured map, that means keys as
        //string and values as collection + string.
        //The ObjectMapper creates this map.
        ObjectMapper mapper = new ObjectMapper();
        meta.setData(mapper.convertValue(dataset, Map.class));
        
        //The render-method of the template creates the final PDF.
        //The PDF is output to the current working directory.
        //The file name is derived from the UsageTemplate class.
        try {
            byte[] data = Service.render(ExampleTemplate.class, meta);
            File output = new File(UsageTemplate.class.getSimpleName() + ".pdf");
            Files.write(output.toPath(), data, StandardOpenOption.CREATE);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    //Simulation of the data access layer as delegate.
    private static class ExampleDelegate {
        
        static List<ExampleEntity> list() {
            return new ArrayList<ExampleEntity>() {{
                add(new ExampleEntity());
            }};
        }
    }
    
    //Simulation of the entity.
    private static class ExampleEntity {
        
        Object outlet = new Object() {
            String name = "Jane Doe Toys Limited";
            String street = "Western Road";
            String location = "GB BN1 2NW Brighton";
            String phone = "+44 1234 05678 0";
            String fax = "+44 1234 05678 1";
            String email = "mail@outlet.local";
            String websiteUrl = "https://outlet.local";
            
            public String getName() {
                return this.name;
            }
            public String getStreet() {
                return this.street;
            }
            public String getLocation() {
                return this.location;
            }
            public String getPhone() {
                return this.phone;
            }
            public String getFax() {
                return this.fax;
            }
            public String getEmail() {
                return this.email;
            }
            public String getWebsiteUrl() {
                return this.websiteUrl;
            }
        };
        
        public Object getOutlet() {
            return this.outlet;
        }
        
        List<Object> articles = new ArrayList<Object>() {{
            add(new Object() {
                String title = "Example A-1";
                String articleNumber = "A-1";
                String price = "123.00 GBP";
                
                public String getTitle() {
                    return this.title;
                }
                public String getArticleNumber() {
                    return this.articleNumber;
                }
                public String getPrice() {
                    return this.price;
                }
            });
            add(new Object() {
                String title = "Example B-2";
                String articleNumber = "B-2";
                String price = "234.00 GBP";
                
                public String getTitle() {
                    return this.title;
                }
                public String getArticleNumber() {
                    return this.articleNumber;
                }
                public String getPrice() {
                    return this.price;
                }
            });
            add(new Object() {
                String title = "Example C-3";
                String articleNumber = "C-3";
                String price = "345.00 GBP";
                
                public String getTitle() {
                    return this.title;
                }
                public String getArticleNumber() {
                    return this.articleNumber;
                }
                public String getPrice() {
                    return this.price;
                }
            });
        }};
        
        public Object getArticles() {
            return this.articles;
        }
    }
    
    //The template is derived from the standard template and therefore requires
    //only a annotation. Optionally, the path from the template can also be
    //specified if the template is not in the template implementation.
    //Base determines where in the ClassPath the resources (fonts, images,
    //styles, ...) for PDF creation are located.
    @Resources(base="/pdf")
    public static class ExampleTemplate extends Template {
    }
}