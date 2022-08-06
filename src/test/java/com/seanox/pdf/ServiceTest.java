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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for Service.<br>
 * <br>
 * ServiceTest 4.2.0 20220806<br>
 * Copyright (C) 2022 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
public class ServiceTest {

    private static String normalizePath(final String path)
            throws Exception {
        final var method = Service.Template.class.getDeclaredMethod("normalizePath", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, path);
    }

    @Test
    void testAcceptance_1()
            throws Exception {
        final var content = TestDataReader.readTestContent("testAcceptance_1.txt");
        final var lines = content.split("\\R");
        for (var loop = 0; loop < lines.length; loop += 2)
            Assertions.assertEquals(lines[loop + 1], normalizePath(lines[loop]));
    }

    @Test
    void testAcceptance_2()
            throws Exception {
        final var content = TestDataReader.readTestContent("testAcceptance_2.txt");
        final var lines = content.split("\\R");
        for (var loop = 0; loop < lines.length; loop += 2) {
            Assertions.assertEquals(lines[loop + 1], normalizePath(lines[loop]));
        }
    }
}