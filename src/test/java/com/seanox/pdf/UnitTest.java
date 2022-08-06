/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 * Diese Software unterliegt der Version 2 der Apache License.
 *
 * PDF Service
 * Copyright (C) 2022 Seanox Software Solutions
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

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import com.seanox.pdf.Service.Meta;
import com.seanox.pdf.Service.Template;
import com.seanox.pdf.Service.Template.Resources;
import com.seanox.pdf.example.UsageTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Unit test for the PDF Service and Tools.<br>
 * <br>
 * UnitTest 4.2.0 20220806<br>
 * Copyright (C) 2022 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
@ExtendWith(UnitTest.Watcher.class)
class UnitTest {
    
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

    static class Watcher implements AfterTestExecutionCallback, AfterAllCallback {
        
        private boolean failed;
        
        @Override
        public void afterTestExecution(final ExtensionContext context) {
            if (context.getExecutionException().isPresent())
                this.failed = true;
        }

        @Override
        public void afterAll(final ExtensionContext context)
                throws Exception {
            if (this.failed
                    && UnitTest.DEBUG)
                Runtime.getRuntime().exec("cmd /C start " + TEMP);
        }
    }

    @BeforeAll
    static void init()
            throws IOException {
        if (TEMP.exists())
            Files.walk(TEMP.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        TEMP.mkdirs();
    }

    private static void validatePreviewPdf(final File previewFile)
            throws Exception {
        final var masterFile = new File(TEMP, previewFile.getName()).getCanonicalFile();
        final var compareFile = new File(masterFile.getParentFile(), masterFile.getName().replaceAll("_preview\\.pdf$", ".pdf")).getCanonicalFile();
        System.out.printf("\tCompare:%n%s%n%s%n%s%n",
                previewFile.getCanonicalFile(), masterFile, compareFile);
        if (!TEMP.equals(previewFile.getParentFile())) {
            Files.copy(previewFile.toPath(), masterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            final var compareRootSourceFile = new File(ROOT, compareFile.getName());
            if (!compareRootSourceFile.exists()) {
                final var compareSourceFile = new File(previewFile.getParentFile(), compareFile.getName());
                if (compareSourceFile.exists())
                    Files.move(compareSourceFile.toPath(), compareFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else Files.move(compareRootSourceFile.toPath(), compareFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Assertions.assertTrue(previewFile.exists(), previewFile.toString());
        Assertions.assertTrue(masterFile.exists(), masterFile.toString());
        Assertions.assertTrue(compareFile.exists(), compareFile.toString());
        Assertions.assertTrue(compareFile.lastModified() >= previewFile.lastModified());
        Assertions.assertTrue(compareFile.lastModified() > masterFile.lastModified());
        final var deltaFiles = Compare.compare(masterFile, compareFile);
        System.out.println("\tDifferences:");
        if (Objects.nonNull(deltaFiles))
            Arrays.stream(deltaFiles).forEach(System.out::println);
        else System.out.println("none");
        Assertions.assertNull(deltaFiles, previewFile.toString());
        Assertions.assertEquals(masterFile.length(), compareFile.length(), masterFile.toString());
    }

    private static boolean compareImages(final File master, final File compare)
            throws Exception {
        final var masterImage = ImageIO.read(master);
        final var compareImage = ImageIO.read(compare);
        final var method = Compare.class.getDeclaredMethod("compareImage", BufferedImage.class, BufferedImage.class);
        method.setAccessible(true);
        final var deltaImage = (BufferedImage)method.invoke(null, masterImage, compareImage);
        if (Objects.isNull(deltaImage))
            return true;
        final var deltaTimestamp = String.format("%tY%<tm%<td%<tH%<tM%<tS", new Date());
        final var deltaName = "_diffs_" + deltaTimestamp;
        final var deltaFile = new File(compare.getParentFile(), compare.getName().replaceAll("\\.\\w+$", deltaName + ".png"));
        ImageIO.write(deltaImage, "png", deltaFile);
        return false;
    }

    @Test
    void checkTemplateGeneration()
            throws Exception {

        Preview.main("src/test/resources/pdf/*.html", "@resourceS");

        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/report_preview.pdf"));

        String master;

        master = "src/test/resources/pdf/reportDiffs_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master));
        var diffs = Compare.compare(new File(TEMP, new File(master).getName()), new File(TEMP, "report_preview.pdf"));
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

        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/articleC_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/articleB_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/articleA_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/articleIncludeA_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/articleIncludeC_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/articleIncludeX_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "target/test-classes/com/seanox/pdf/example/ArticleSingleTemplate_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "target/test-classes/com/seanox/pdf/example/ArticleSingleIncludeTemplate_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "target/test-classes/com/seanox/pdf/example/ArticleMultiTemplate_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/compareA_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/compareB_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/overlays_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/encoding_utf_8_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/generator_preview.pdf"));
    }

    @Test
    void checkCompatibility()
            throws Exception {
        final var outputFile = new File(TEMP, "compatibility.pdf");
        final var sourceContent = new String(UnitTest.class.getResourceAsStream("/pdf/compatibility.html").readAllBytes());
        Files.deleteIfExists(outputFile.toPath());
        try (final var outputStream = new FileOutputStream(outputFile)) {
            final var builder = new PdfRendererBuilder();
            builder.withHtmlContent(sourceContent, "/");
            builder.toStream(outputStream);
            builder.run();
        }
        final var master = "src/test/resources/pdf/compatibility_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master));
    }

    @Test
    void checkTemplatesWithErrors_1() {
        var throwable = Assertions.assertThrows(Template.TemplateResourceNotFoundException.class, () ->
                Preview.execute(new File(ROOT, "src/test/resources/pdf/articleIncludeB.html")));
        Assertions.assertTrue(throwable.toString().replace("\\", "/") .contains("/pdf/pdf/articleA.html"));
    }

    @Test
    void checkTemplatesWithErrors_2() {
        var throwable = Assertions.assertThrows(Template.TemplateResourceNotFoundException.class, () ->
                Preview.execute(new File(ROOT, "src/test/resources/pdf/articleIncludeE.html")));
        Assertions.assertTrue(throwable.toString().contains("articleIncludeE_1.html"));
    }

    @Test
    void checkTemplatesWithErrors_3() {
        var throwable = Assertions.assertThrows(Service.Template.TemplateException.class, () ->
                Preview.execute(new File(ROOT, "src/test/resources/pdf/articleIncludeF.html")));
        Assertions.assertTrue(throwable.toString().contains("Recursion found in:"));
        Assertions.assertTrue(throwable.toString().contains("articleIncludeF_2.html"));
    }

    @Test
    void checkUsageTemplate()
            throws Exception {
        UsageTemplate.main();
        final var master = "src/test/resources/com/seanox/pdf/example/UsageTemplate_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master).getCanonicalFile());
    }

    @Test
    void checkMarkupStructureEmpty()
            throws Exception {
        Preview.main("src/test/resources/pdf/structure_**empty.html");
        final var resourcePath = "src/test/resources/pdf/";
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_content_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_content_footer_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_footer_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_content_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_content_footer_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_footer_empty_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_empty_preview.pdf"));
    }

    @Test
    void checkMarkupStructureNotEmpty()
            throws Exception {
        Preview.main("src/test/resources/pdf/structure_**.html");
        final var resourcePath = "src/test/resources/pdf/";
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_content_footer_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_content_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_footer_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_content_footer_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_content_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_footer_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_header_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, resourcePath
                + "structure_body_preview.pdf"));
    }

    @Resources(base="/pdf")
    static class DuplicateTemplate extends com.seanox.pdf.Template {
    }
    
    @Test
    void checkDuplicateTemplate()
            throws Exception {
        
        final var meta = new Meta();

        final var data = new TreeMap<String, Object>();
        data.put("x", "1");
        data.put("X", "2");
        meta.setData(data);

        final var statics = new TreeMap<String, String>();
        statics.put("x", "A");
        statics.put("X", "B");
        meta.setStatics(statics);

        final var output = new File(TEMP, "DuplicateTemplate.pdf");
        final var pdf = Service.render(DuplicateTemplate.class, meta);
        Files.write(output.toPath(), pdf, StandardOpenOption.CREATE);

        final var master = "src/test/resources/com/seanox/pdf/DuplicateTemplate_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master));
    }

    @Test
    void checkCompare()
            throws Exception {

        Preview.main("src/test/resources/pdf/compare*.html");

        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/compareA_preview.pdf"));
        UnitTest.validatePreviewPdf(new File(ROOT, "src/test/resources/pdf/compareB_preview.pdf"));

        final var diffs = Compare.compare(new File(TEMP, "compareA.pdf"), new File(TEMP, "compareB.pdf"));
        Assertions.assertNotNull(diffs);
        Assertions.assertEquals(1, diffs.length);

        final String master = "src/test/resources/pdf/compareB_diffs_page_1.png";
        Files.copy(Paths.get(master), new File(TEMP, new File(master).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assertions.assertTrue(UnitTest.compareImages(new File(TEMP, new File(master).getName()), diffs[0]));
    }
    
    @Resources(base="/pdf")
    static class ExistsTemplate extends com.seanox.pdf.Template {
    }    
    
    @Test
    void checkExistsTemplate()
            throws Exception {
        
        final var statics = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L; {
            put("VALUE_EMPTY", "");
            put("VALUE_BLANK", " ");
            put("VALUE_NULL", null);
            put("VALUE_NOT_EMPTY", "1");
            put("VALUE_NOT_BLANK", "2");
            put("VALUE_NOT_NULL", "3");
        }};

        final var data = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L; {
            put("VALUE_EMPTY", "");
            put("VALUE_BLANK", " ");
            put("VALUE_NULL", null);
            put("VALUE_NOT_EMPTY", "1");
            put("VALUE_NOT_BLANK", "2");
            put("VALUE_NOT_NULL", "3");
            
            put("COLLECTION_NULL", null);
            put("COLLECTION_EMPTY", new ArrayList<>());
            put("COLLECTION_NOT_EMPTY", Collections.singletonList(statics));

            put("MAP_NULL", null);
            put("MAP_EMPTY", new HashMap<>());
            put("MAP_NOT_EMPTY", statics);
        }};

        final var output = new File(TEMP, "ExistsTemplate.pdf");
        final var pdf = Service.render(ExistsTemplate.class, new Service.Meta(data, statics));
        Files.write(output.toPath(), pdf, StandardOpenOption.CREATE);
        final var master = "src/test/resources/com/seanox/pdf/ExistsTemplate_preview.pdf";
        UnitTest.validatePreviewPdf(new File(ROOT, master).getCanonicalFile());
    }
}