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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.openhtmltopdf.util.XRLog;
import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.example.UsageTemplate;

/** 
 * Unit test for the PDF Server and Tools.<br>
 * <br>
 * Test-Runtime Configuration:
 * <ul>
 *   <li>Oracle Java 11</li>
 *   <li>JUnit 5</li>
 * </ul>
 * <br>
 * UnitTest 3.7.0 20200827<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.7.0 20200827
 */
@SuppressWarnings("javadoc")
@ExtendWith(UnitTest.Watcher.class)
public class UnitTest {
    
    private final static File ROOT = new File(".");
    
    private final static File TEMP = new File(ROOT, "temp");
    
    private final static boolean DEBUG = false;
    
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

    private static class Watcher implements AfterTestExecutionCallback, AfterAllCallback {
        
        private boolean failed;
        
        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            if (context.getExecutionException().isPresent())
                this.failed = true;
        }

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            if (this.failed
                    && UnitTest.DEBUG)
                Runtime.getRuntime().exec("cmd /C start " + TEMP);
        }
    }
    
    @BeforeAll
    public static void init()
            throws IOException {
        
        if (TEMP.exists())
            Files.walk(TEMP.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TEMP.mkdirs();
    }

    private static void validatePreviewPdf(File master, long time)
            throws IOException {
        
        Assertions.assertTrue(master.exists(), master.toString());
        if (time >= 0)
            Assertions.assertTrue(master.lastModified() < time);
        File compare = new File(master.getParentFile(), master.getName().replaceAll("_preview\\.pdf$", ".pdf"));
        Assertions.assertTrue(compare.exists(), master.toString());
        if (time >= 0)
            Assertions.assertTrue(compare.lastModified() > time, master.toString());
        Files.copy(master.toPath(), new File(TEMP, master.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.move(compare.toPath(), new File(TEMP, compare.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertNull(Compare.compare(new File(TEMP, master.getName()), new File(TEMP, compare.getName())), master.toString());
        Assertions.assertEquals(new File(TEMP, master.getName()).length(), new File(TEMP, compare.getName()).length(), master.toString());
    }

    private static void validatePreviewPdf(File master)
            throws IOException {
        UnitTest.validatePreviewPdf(master, -1);
    }
    
    private static boolean compareImages(File master, File compare)
            throws Exception {

        BufferedImage masterImage = ImageIO.read(master);
        BufferedImage compareImage = ImageIO.read(compare);
        Method method = Compare.class.getDeclaredMethod("compareImage", BufferedImage.class, BufferedImage.class);
        method.setAccessible(true);
        BufferedImage deltaImage = (BufferedImage)method.invoke(null, masterImage, compareImage);
        if (deltaImage == null)
            return true;
        String deltaTimestamp = String.format("%tY%<tm%<td%<tH%<tM%<tS", new Date()); 
        String deltaName = "_diffs_" + deltaTimestamp;
        File deltaFile = new File(compare.getParentFile(), compare.getName().replaceAll("\\.\\w+$", deltaName + ".png"));
        ImageIO.write(deltaImage, "png", deltaFile);
        return false;
    }

    @Test
    public void checkTemplateGeneration()
            throws Exception {
        
        String master;
        
        long time = System.currentTimeMillis();
        
        Preview.main("./src/test/resources/pdf/*.html");
        
        master = "src/test/resources/pdf/report_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);

        master = "src/test/resources/pdf/reportDiffs_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        File[] diffs = Compare.compare(new File(TEMP, new File(master).getName()), new File(TEMP, "report_preview.pdf"));
        Assertions.assertNotNull(diffs);
        Assertions.assertEquals(3, diffs.length);
        master = "src/test/resources/pdf/reportDiffs_diffs_page_1.png";
        Files.copy(Paths.get(master), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(UnitTest.compareImages(new File(TEMP, new File(master).getName()), diffs[0]));
        master = "src/test/resources/pdf/reportDiffs_diffs_page_2.png";
        Files.copy(Paths.get(master), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(UnitTest.compareImages(new File(TEMP, new File(master).getName()), diffs[1]));
        master = "src/test/resources/pdf/reportDiffs_diffs_page_3.png";
        Files.copy(Paths.get(master), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(UnitTest.compareImages(new File(TEMP, new File(master).getName()), diffs[2]));

        master = "src/test/resources/pdf/articleC_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);        

        master = "src/test/resources/pdf/articleB_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/articleA_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);

        master = "src/test/resources/pdf/articleIncludeA_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);

        master = "src/test/resources/pdf/articleIncludeC_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/articleIncludeX_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "target/test-classes/com/seanox/pdf/example/ArticleSingleTemplate_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "target/test-classes/com/seanox/pdf/example/ArticleSingleIncludeTemplate_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "target/test-classes/com/seanox/pdf/example/ArticleMultiTemplate_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/compareA_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/compareB_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/overlays_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/encoding_utf_8_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/generator_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master), time);        
    }
    
    @Test
    public void checkTemplatesWithErrors() {
        
        try {
            Preview.execute(new File(ROOT, "src/test/resources/pdf/articleIncludeB.html"));
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertTrue(exception instanceof Template.TemplateResourceNotFoundException);
            Assertions.assertTrue(exception.toString().replace("\\", "/") .contains("/pdf/pdf/articleA.html"));
        }
        
        try {
            Preview.execute(new File(ROOT, "src/test/resources/pdf/articleIncludeE.html"));
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertTrue(exception instanceof Template.TemplateResourceNotFoundException);
            Assertions.assertTrue(exception.toString().contains("articleIncludeE_1.html"));
        }
        
        try {
            Preview.execute(new File(ROOT, "src/test/resources/pdf/articleIncludeF.html"));
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertTrue(exception instanceof Service.Template.TemplateException);
            Assertions.assertTrue(exception.toString().contains("Recursion found in:"));
            Assertions.assertTrue(exception.toString().contains("articleIncludeF_2.html"));
        }    
    }
    
    @Test
    public void checkUsageTemplate()
            throws Exception {
        
        String master;
        String compare;

        long time = System.currentTimeMillis();
        
        UsageTemplate.main();
        master = "src/test/resources/com/seanox/pdf/example/UsageTemplate_preview.pdf";
        Assertions.assertTrue(new File(ROOT, master).exists());
        Assertions.assertTrue(new File(ROOT, master).lastModified() < time);
        compare = "UsageTemplate.pdf";
        Files.copy(new File(ROOT, master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.move(new File(ROOT, compare).toPath(), new File(TEMP, new File(compare).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(new File(TEMP, compare).exists());
        Assertions.assertTrue(new File(TEMP, compare).lastModified() > time);
        Assertions.assertNull(Compare.compare(new File(TEMP, new File(master).getName()), new File(TEMP, compare)));
        Assertions.assertEquals(new File(ROOT, master).length(), new File(TEMP, compare).length());
    }
    
    @Test
    public void checkMarkupStructureEmpty()
            throws IOException {
        
        for (String master : new String[] {
                "structure_body_content_empty_preview.pdf",
                "structure_body_content_footer_empty_preview.pdf",
                "structure_body_empty_preview.pdf",
                "structure_body_footer_empty_preview.pdf",
                "structure_body_header_content_empty_preview.pdf",
                "structure_body_header_content_footer_empty_preview.pdf",
                "structure_body_header_empty_preview.pdf",
                "structure_body_header_footer_empty_preview.pdf",
                "structure_empty_preview.pdf"}) {
            master = "src/test/resources/pdf/" + master;
            UnitTest.validatePreviewPdf(new File(ROOT, master));
        }
    }
    
    @Test
    public void checkMarkupStructureNotEmpty()
            throws IOException {
        
        for (String master : new String[] {
                "structure_body_content_footer_preview.pdf",
                "structure_body_content_preview.pdf",
                "structure_body_footer_preview.pdf",
                "structure_body_header_content_footer_preview.pdf",
                "structure_body_header_content_preview.pdf",
                "structure_body_header_footer_preview.pdf",
                "structure_body_header_preview.pdf",
                "structure_body_preview.pdf"}) {
            master = "src/test/resources/pdf/" + master;
            UnitTest.validatePreviewPdf(new File(ROOT, master));        
        }
    }
    
    @Resources(base="/pdf")
    static class DuplicateTemplate extends com.seanox.pdf.Template {
    }
    
    @Test
    public void checkDuplicateTemplate()
            throws Exception {
        
        long time = System.currentTimeMillis();
        
        Meta meta = new Meta();
        Map<String, Object> data = new TreeMap<>();
        data.put("x", "1");
        data.put("X", "2");
        meta.setData(data);
        Map<String, String> statics = new TreeMap<>();
        statics.put("x", "A");
        statics.put("X", "B");
        meta.setStatics(statics);
        
        File output = new File(TEMP, "DuplicateTemplate.pdf");
        byte[] pdf = Service.render(DuplicateTemplate.class, meta);
        Files.write(output.toPath(), pdf, StandardOpenOption.CREATE);
        
        String master = "src/test/resources/com/seanox/pdf/DuplicateTemplate_preview.pdf";
        Assertions.assertTrue(new File(ROOT, master).exists());
        Assertions.assertTrue(new File(ROOT, master).lastModified() < time);
        Files.copy(new File(ROOT, master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        String compare = "DuplicateTemplate.pdf";
        Assertions.assertTrue(new File(TEMP, compare).exists());
        Assertions.assertTrue(new File(TEMP, compare).lastModified() > time);
        Assertions.assertNull(Compare.compare(new File(TEMP, new File(master).getName()), new File(TEMP, compare)));
        Assertions.assertEquals(new File(ROOT, master).length(), new File(TEMP, compare).length());
    }

    @Test
    public void checkCompare()
            throws Exception {
        
        File[] diffs = Compare.compare(new File(TEMP, "compareA.pdf"), new File(TEMP, "compareB.pdf"));
        Assertions.assertNotNull(diffs);
        Assertions.assertEquals(1, diffs.length);
        
        String master = "src/test/resources/pdf/compareB_diffs_page_1.png";
        Files.copy(Paths.get(master), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(UnitTest.compareImages(new File(TEMP, new File(master).getName()), diffs[0]));
    }
    
    @Resources(base="/pdf")
    static class ExistsTemplate extends com.seanox.pdf.Template {
    }    
    
    @Test
    public void checkExistsTemplate()
            throws Exception {
        
        long time = System.currentTimeMillis();
        
        Map<String, String> statics = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L; {
            put("VALUE_EMPTY", "");
            put("VALUE_BLANK", " ");
            put("VALUE_NULL", null);
            put("VALUE_NOT_EMPTY", "1");
            put("VALUE_NOT_BLANK", "2");
            put("VALUE_NOT_NULL", "3");
        }};
        
        Map<String, Object> data = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L; {
            put("VALUE_EMPTY", "");
            put("VALUE_BLANK", " ");
            put("VALUE_NULL", null);
            put("VALUE_NOT_EMPTY", "1");
            put("VALUE_NOT_BLANK", "2");
            put("VALUE_NOT_NULL", "3");
            
            put("COLLECTION_NULL", null);
            put("COLLECTION_EMPTY", new ArrayList<>());
            put("COLLECTION_NOT_EMPTY", Arrays.asList(new Map[] {statics}));

            put("MAP_NULL", null);
            put("MAP_EMPTY", new HashMap<>());
            put("MAP_NOT_EMPTY", statics);
        }};
        
        File output = new File(TEMP, "ExistsTemplate.pdf");
        byte[] pdf = Service.render(ExistsTemplate.class, new Service.Meta(data, statics));
        Files.write(output.toPath(), pdf, StandardOpenOption.CREATE);
        
        String master = "src/test/resources/com/seanox/pdf/ExistsTemplate_preview.pdf";
        Assertions.assertTrue(new File(ROOT, master).exists());
        Assertions.assertTrue(new File(ROOT, master).lastModified() < time);
        Files.copy(new File(ROOT, master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        String compare = "ExistsTemplate.pdf";
        Assertions.assertTrue(new File(TEMP, compare).exists());
        Assertions.assertTrue(new File(TEMP, compare).lastModified() > time);
        Assertions.assertNull(Compare.compare(new File(TEMP, new File(master).getName()), new File(TEMP, compare)));
        Assertions.assertEquals(new File(ROOT, master).length(), new File(TEMP, compare).length());
    }
}