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
 * The resources (css, imgaes, ...) are in the ClassPath /pdf/... and are used
 * in the template relative.<br>
 * <br>
 * UsageTemplate 1.0 20200229<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 1.0 20200229
 */
public class UsageTemplate {
    
    public static void main(String[] options) {
 
        Map<String, String> statics = new HashMap<String, String>() {{
            put("ARTICLE_NUMBER", "Article number");
            put("ARTICLE_PRICE", "Price");
            put("ADDRESS_TEL", "Tel");
            put("ADDRESS_FAX", "Fax");
            put("ADDRESS_E_MAIL", "E-Mail");
            put("ADDRESS_WEB", "Web");
        }};
        
        List<ExampleDataBean> dataset = new ArrayList<ExampleDataBean>() {{
            add(new ExampleDataBean());
        }};
        
        ObjectMapper mapper = new ObjectMapper();
        
        Meta meta = new Meta();
        meta.setStatics(statics);
        meta.setDataset(mapper.convertValue(dataset, List.class));
        
        try {
            byte[] data = Service.generate(new ExampleTempate(), meta);
            File output = new File(UsageTemplate.class.getSimpleName() + ".pdf");
            Files.write(output.toPath(), data, StandardOpenOption.CREATE);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    private static class ExampleDataBean {
        
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
    
    @Resources(base="/pdf")
    private static class ExampleTempate extends Template {
    }
}