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
package com.seanox.pdf.example.data;

@SuppressWarnings("javadoc")
public class Article {
    
    private String headline;
    private String articleNumber;
    private String colorText;
    private String materialText;
    private String images;
    private ArticlePrice articlePrice;

    private String description;
    
    public String getHeadline() {
        return this.headline;
    }
    public void setHeadline(String headline) {
        this.headline = headline;
    }
    public String getArticleNumber() {
        return this.articleNumber;
    }
    public void setArticleNumber(String number) {
        this.articleNumber = number;
    }
    public String getColorText() {
        return this.colorText;
    }
    public void setColorText(String colorText) {
        this.colorText = colorText;
    }
    public String getMaterialText() {
        return this.materialText;
    }
    public void setMaterialText(String materialText) {
        this.materialText = materialText;
    }
    public String getImages() {
        return this.images;
    }
    public void setImages(String images) {
        this.images = images;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public ArticlePrice getArticlePrice() {
        return this.articlePrice;
    }
    public void setArticlePrice(ArticlePrice articlePrice) {
        this.articlePrice = articlePrice;
    }

    public static class ArticlePrice {
        
        private String netText;
        private String grossText;
        
        public String getNetText() {
            return this.netText;
        }
        public void setNetText(String netText) {
            this.netText = netText;
        }
        public String getGrossText() {
            return this.grossText;
        }
        public void setGrossText(String grossText) {
            this.grossText = grossText;
        }
    }
}