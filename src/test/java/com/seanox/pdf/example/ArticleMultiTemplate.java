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