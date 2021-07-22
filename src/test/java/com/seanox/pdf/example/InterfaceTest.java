/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2021 Seanox Software Solutions
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

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template;

/**
 * Not a real test class.
 * Here the implementation and use of the Template-API shall be tested and
 * protected by the compiler..<br>
 * <br>
 * InterfaceTest 4.0.0 20200614<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.0.0 20200614
 */
class InterfaceTest extends Template {

    @Override
    protected String getBasePath() {
        return super.getBasePath();
    }  
    
    @Override
    protected URI getBase()
            throws Exception {
        return super.getBase();
    }
    
    @Override
    protected String getSourcePath() {
        return super.getSourcePath();
    }        
    
    @Override
    protected URI getSource()
            throws Exception {
        return super.getSource();
    }

    @Override
    protected InputStream getSourceStream()
            throws Exception {
        return super.getSourceStream();
    }
    
    @Override
    protected URI getResource(String resource)
            throws Exception {
        return super.getResource(resource);
    }

    @Override
    protected InputStream getResourceStream(String resource)
            throws Exception {
        return super.getResourceStream(resource);
    }
    
    @Override
    protected String getMarkup()
            throws Exception {
        return super.getMarkup();
    }

    @Override
    protected Map<String, Object> getPreviewData() {
        return null;
    }

    @Override
    protected Map<String, String> getPreviewStatics() {
        return null;
    }
    
    @Override
    protected byte[] getPreview()
            throws Exception {
        return super.getPreview();
    }

    @Override
    protected String generate(String markup, Type type, Meta meta) {
        return null;
    }
    
    @Override
    protected byte[] render(Meta meta)
            throws Exception {
        return super.render(meta);
    }
    
    private void dummy()
            throws Exception {

        Template.normalizePath(null);
        Template.merge(null);

        Multiplex multiplex = Multiplex.demux(null);
        multiplex.getHeader();
        multiplex.getContent();
        multiplex.getFooter();
        
        Resources resources = InterfaceTest.class.getAnnotation(Resources.class);
        resources.base();
        resources.template();
    }
     
    @Override
    public String toString() {
        return super.getSourcePath();
    }
}