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
public class Report {
    
    private Data[] data;
    
    public Data[] getData() {
        return this.data;
    }
    public void setData(Data[] data) {
        this.data = data;
    }

    static class Data {
        
        private String a;
        private String b;
        private String c;
        private String d;
        private String e;
        private String f;
        private String g;
        private String h;
        
        public String getA() {
            return this.a;
        }
        public void setA(String a) {
            this.a = a;
        }
        public String getB() {
            return this.b;
        }
        public void setB(String b) {
            this.b = b;
        }
        public String getC() {
            return this.c;
        }
        public void setC(String c) {
            this.c = c;
        }
        public String getD() {
            return this.d;
        }
        public void setD(String d) {
            this.d = d;
        }
        public String getE() {
            return this.e;
        }
        public void setE(String e) {
            this.e = e;
        }
        public String getF() {
            return this.f;
        }
        public void setF(String f) {
            this.f = f;
        }
        public String getG() {
            return this.g;
        }
        public void setG(String g) {
            this.g = g;
        }
        public String getH() {
            return this.h;
        }
        public void setH(String h) {
            this.h = h;
        }
    }
}