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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

/**
 * Unit test for Template.<br>
 * <br>
 * GeneratorTest 4.2.0 20220806<br>
 * Copyright (C) 2022 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
public class TemplateTest {

    private Map<String, Object> getPreviewData(final String properties)
            throws Exception {
        return (new Template() {
            protected Properties getPreviewProperties()
                    throws Exception {
                return TestDataReader.readTestDataProperties(properties);
            }
        }).getPreviewData();
    }

    @Test
    void testAcceptance_1()
            throws Exception {
        final var dataMap = this.getPreviewData("testAcceptance_1.properties");
        final var objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        final var jsonString = objectWriter.writeValueAsString(dataMap);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_1.json"), jsonString);
    }

    @Test
    void testAcceptance_2()
            throws Exception {
        final var dataMap = this.getPreviewData("testAcceptance_2.properties");
        final var objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        final var jsonString = objectWriter.writeValueAsString(dataMap);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_2.json"), jsonString);
    }

    @Test
    void testAcceptance_3() {
        final var throwable = Assertions.assertThrows(com.seanox.pdf.Template.PreviewDataParserException.class, () ->
                this.getPreviewData("testAcceptance_3.properties"));
        Assertions.assertEquals("Invalid key: 0.1.2.3.4", throwable.getMessage());
    }

    @Test
    void testAcceptance_4() {
        final var throwable = Assertions.assertThrows(com.seanox.pdf.Template.PreviewDataParserException.class, () ->
                this.getPreviewData("testAcceptance_4.properties"));
        Assertions.assertEquals("Invalid key: $.1.$.1", throwable.getMessage());
    }

    @Test
    void testAcceptance_5() {
        final var throwable = Assertions.assertThrows(com.seanox.pdf.Template.PreviewDataParserException.class, () ->
                this.getPreviewData("testAcceptance_5.properties"));
        Assertions.assertEquals("Invalid key index: $$$[1].$", throwable.getMessage());
    }
}