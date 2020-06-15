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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.openhtmltopdf.util.XRLog;
import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.example.UsageTemplate;

/** 
 * Wrapper to run the {@link Preview} with the test classes and resources.<br>
 * <br>
 * PreviewTest 3.6.0 20200615<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.6.0 20200615
 */
@RunWith(JUnitPlatform.class)
@SuppressWarnings("javadoc")
public class PreviewTest {
    
    private final static File ROOT = new File(".");
    
    private final static File TEMP = new File(ROOT, "temp");
    
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

    @Test
    public void test01() {

        //test for the correct dependencies in the project / classpath
        Class<?> dataAccessException = org.springframework.dao.DataAccessException.class;
        Assertions.assertNotNull(dataAccessException);
    }
    
    private static void validatePreviewPdf(File master, long time)
            throws IOException {
        
        Assertions.assertTrue(master.exists());
        if (time >= 0)
            Assertions.assertTrue(master.lastModified() < time);
        File compare = new File(master.getParentFile(), master.getName().replaceAll("_preview\\.pdf$", ".pdf"));
        Assertions.assertTrue(compare.exists());
        if (time >= 0)
            Assertions.assertTrue(compare.lastModified() > time);
        Files.copy(master.toPath(), new File(TEMP, master.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.move(compare.toPath(), new File(TEMP, compare.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertNull(Compare.compare(new File(TEMP, master.getName()), new File(TEMP, compare.getName())));
        Assertions.assertEquals(new File(TEMP, master.getName()).length(), new File(TEMP, compare.getName()).length());
    }

    private static void validatePreviewPdf(File master)
            throws IOException {
        PreviewTest.validatePreviewPdf(master, -1);
    }
    
    private static boolean compareImages(File master, File compare)
            throws IOException {
        
        if (master.length()
                != compare.length())
            return false;
        
        BufferedImage masterImage = ImageIO.read(master);
        BufferedImage compareImage = ImageIO.read(compare);
        if (masterImage.getHeight() != compareImage.getHeight()
                || masterImage.getWidth() != compareImage.getWidth())
            return false;
        
        for (int y = 0; y < masterImage.getHeight(); y++) {
            for (int x = 0; x < masterImage.getWidth(); x++) {
                if (masterImage.getRGB(x, y)
                        != compareImage.getRGB(x, y))
                    return false;
            }
        }
        
        return true;
    }

    @Test
    public void test02()
            throws Exception {
        
        String master;
        
        long time = System.currentTimeMillis();
        
        Preview.main(new String[] {"./src/test/resources/pdf/*.html"});
        
        master = "src/test/resources/pdf/report_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);

        master = "src/test/resources/pdf/reportDiffs_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        File[] diffs = Compare.compare(new File(TEMP, new File(master).getName()), new File(TEMP, "report_preview.pdf"));
        Assertions.assertNotNull(diffs);
        Assertions.assertEquals(3, diffs.length);
        master = "src/test/resources/pdf/reportDiffs_diffs_page_1.png";
        Files.copy(new File(master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(PreviewTest.compareImages(new File(TEMP, new File(master).getName()), diffs[0]));
        master = "src/test/resources/pdf/reportDiffs_diffs_page_2.png";
        Files.copy(new File(master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(PreviewTest.compareImages(new File(TEMP, new File(master).getName()), diffs[1]));
        master = "src/test/resources/pdf/reportDiffs_diffs_page_3.png";
        Files.copy(new File(master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(PreviewTest.compareImages(new File(TEMP, new File(master).getName()), diffs[2]));

        master = "src/test/resources/pdf/articleC_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);        

        master = "src/test/resources/pdf/articleB_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/articleA_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);

        master = "src/test/resources/pdf/articleIncludeA_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);

        master = "src/test/resources/pdf/articleIncludeC_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/articleIncludeX_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "target/test-classes/com/seanox/pdf/example/ArticleSingleTemplate_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "target/test-classes/com/seanox/pdf/example/ArticleSingleIncludeTemplate_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "target/test-classes/com/seanox/pdf/example/ArticleMultiTemplate_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/compareA_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/compareB_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
        
        master = "src/test/resources/pdf/overlays_preview.pdf";
        PreviewTest.validatePreviewPdf(new File(ROOT, master), time);
    }
    
    @Test
    public void test03() {
        
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
    public void test04()
            throws IOException {
        
        String master;
        String compare;

        long time = System.currentTimeMillis();
        
        UsageTemplate.main(null);
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
    public void test05()
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
            PreviewTest.validatePreviewPdf(new File(ROOT, master));
        }
    }
    
    @Test
    public void test06()
            throws IOException {
        
        long time = System.currentTimeMillis();
        
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
            PreviewTest.validatePreviewPdf(new File(ROOT, master));        
        }
    }
    
    @Test
    public void test07()
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
    public void test08()
            throws Exception {
        
        File[] diffs = Compare.compare(new File(TEMP, "compareA.pdf"), new File(TEMP, "compareB.pdf"));
        Assertions.assertNotNull(diffs);
        Assertions.assertEquals(1, diffs.length);
        
        String master = "src/test/resources/pdf/compareB_diffs_page_1.png";
        Files.copy(new File(master).toPath(), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(PreviewTest.compareImages(new File(TEMP, new File(master).getName()), diffs[0]));
    }

    @Resources(base="/pdf")
    public static class DuplicateTemplate extends com.seanox.pdf.Template {
    }
}