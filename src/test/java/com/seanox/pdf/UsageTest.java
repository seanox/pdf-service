/**
 * PDF Service
 * Copyright (C) 2026 Seanox Software Solutions
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UsageTest {
    
    private static String callMain(final String... options) {
        final var output = System.out;
        final var buffer = new ByteArrayOutputStream();
        try (final var cache = new PrintStream(buffer)) {
            System.setOut(cache);
            Usage.main(options);
        }
        System.setOut(output);
        return buffer.toString();
    } 

    @Test
    void testAcceptance_1()
            throws Exception {
        
        final var output = UsageTest.callMain();
        Assertions.assertFalse(output.isEmpty(), output);
        Assertions.assertFalse(output.isBlank(), output);
        Assertions.assertTrue(output.contains("Seanox PDF Tools Usage"), output);
        Assertions.assertTrue(output.contains("Seanox PDF Comparator"), output); 
        Assertions.assertTrue(output.contains("Seanox PDF Runner"), output);     
        Assertions.assertTrue(output.contains("Seanox PDF Preview"), output); 
        
        Assertions.assertEquals(output, UsageTest.callMain(null));
        Assertions.assertEquals(output, UsageTest.callMain(""));
        Assertions.assertEquals(output, UsageTest.callMain("", ""));
        Assertions.assertEquals(output, UsageTest.callMain("1", "2", "3"));
    }
}