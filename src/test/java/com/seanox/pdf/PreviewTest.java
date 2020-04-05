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
package com.seanox.pdf;

import java.io.File;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.openhtmltopdf.util.XRLog;
import com.seanox.pdf.Service.Template;

/** 
 * Wrapper to run the {@link Preview} with the test classes and resources.<br>
 * <br>
 * PreviewTest 3.3.1 20200316<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.3.1 20200316
 */
public class PreviewTest {
    
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
        
        XRLog.setLoggingEnabled(false);
    }
    
    @Test
    public void testMain() {
        
        String source;
        String target;

        File root = new File(".");
        long time = System.currentTimeMillis();
        
        Preview.main(new String[] {"./src/test/resources/pdf/*.html"});
        
        source = "src/test/resources/pdf/report_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());
        
        source = "src/test/resources/pdf/articleB_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());
        
        source = "src/test/resources/pdf/articleA_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());
        
        source = "src/test/resources/pdf/articleIncludeA_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());
        
        source = "src/test/resources/pdf/articleIncludeC_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());
        
        source = "src/test/resources/pdf/articleIncludeX_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());

        source = "target/test-classes/com/seanox/pdf/example/ArticleSingleTemplate_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());
        
        source = "target/test-classes/com/seanox/pdf/example/ArticleSingleIncludeTemplate_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());

        source = "target/test-classes/com/seanox/pdf/example/ArticleMultiTemplate_preview.pdf";
        Assertions.assertTrue(new File(root, source).exists());
        Assertions.assertTrue(new File(root, source).lastModified() < time);
        target = source.replaceAll("_preview\\.pdf$", ".pdf");
        Assertions.assertTrue(new File(root, target).exists());
        Assertions.assertTrue(new File(root, target).lastModified() > time);
        Assertions.assertEquals(new File(root, source).length(), new File(root, target).length());

        try {
            Preview.execute(new File("src/test/resources/pdf/articleIncludeB.html"));
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertTrue(exception instanceof Template.TemplateResourceNotFoundException);
            Assertions.assertTrue(exception.toString().replace("\\", "/") .contains("/pdf/pdf/articleA.html"));
        }
        
        try {
            Preview.execute(new File("src/test/resources/pdf/articleIncludeE.html"));
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertTrue(exception instanceof Template.TemplateResourceNotFoundException);
            Assertions.assertTrue(exception.toString().contains("articleIncludeE_1.html"));
        }
        
        try {
            Preview.execute(new File("src/test/resources/pdf/articleIncludeF.html"));
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertTrue(exception instanceof Service.Template.TemplateException);
            Assertions.assertTrue(exception.toString().contains("Recursion found in:"));
            Assertions.assertTrue(exception.toString().contains("articleIncludeF_2.html"));
        }        
    }
}