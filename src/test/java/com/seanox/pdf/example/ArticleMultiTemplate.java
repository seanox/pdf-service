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

import java.util.Map;

import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Meta.Type;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.Template;

/** 
 * Example of using the PDF service.
 * Template and preview data are in the same package.
 * The resources (css, imgaes, ...) are in the ClassPath /pdf/... and are used
 * in the template relative.
 */
@Resources(base="/pdf")
public class ArticleMultiTemplate extends Template {
    
    @Override
    protected String generate(String markup, Type type, Meta meta) {

        // Replication of the articles on the basis of the preview dataset.
        
        try {
            for (int loop = 1; loop < 5; loop++) {
                Map<String, Object> record;
                if (loop != 0) {
                    record = this.getPreviewData();
                    meta.getDataset().add(record);
                } else record = meta.getDataset().iterator().next();
                Map<String, Object> article = (Map<String, Object>)record.get("article");
                article.replace("articleNumber", article.get("articleNumber") + "-" + loop);
            }
            return super.generate(markup, type, meta);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}