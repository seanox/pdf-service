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
package com.seanox.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Unit test for Generator.<br>
 * <br>
 * Test-Runtime Configuration:
 * <ul>
 *   <li>Oracle Java 11</li>
 *   <li>JUnit 5</li>
 * </ul>
 * <br>
 * GeneratorTest 4.1.0 20210823<br>
 * Copyright (C) 2021 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 4.1.0 20210823
 */
class GeneratorTest {

    private static String readTestContent(final String source)
            throws Exception {
        final StackTraceElement stackTraceElements = new Throwable().getStackTrace()[1];
        final String resourcePath = stackTraceElements.getClassName()
                .replaceAll("\\.[^\\.]+$", "")
                .replace('.', '/');
        final String className = StringUtils.uncapitalize(stackTraceElements.getClassName()
                .replaceAll("(^.*\\.)|(Test$)", ""));
        final ClassLoader classLoader = Class.forName(stackTraceElements.getClassName()).getClassLoader();
        return new String(classLoader.getResourceAsStream(resourcePath + "/" + className + "/" + source).readAllBytes());
    }

    private static Map<String, Object> readTestData(final String source)
            throws Exception {
        return new ObjectMapper().readValue(GeneratorTest.readTestContent(source), HashMap.class);
    }

    @Test
    void testAcceptance_1()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_1_1.txt").getBytes());
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_1_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_2()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_2_1.txt").getBytes());
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_2_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_3()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_1.txt").getBytes());
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_3_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_4()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (final String entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_4_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_5()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int loop = 1; loop < 7; loop++) {
            String charX = Character.toString((char)('A' -1 + loop));
            values.put("case", charX + "1");
            values.put("name", charX + "2");
            values.put("date", charX + "3");
            values.put("size", charX + "4");
            values.put("type", charX + "5");
            values.put("mime", charX + "6");
            buffer.write(generator.extract("file", values));
        }
        values.put("file", buffer.toByteArray());
        generator.set(values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_5_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_6()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_2.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (final String entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_6_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[file]", scopes);
    }

    @Test
    void testAcceptance_7()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (String entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_7_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[file]", scopes);
    }

    @Test
    void testAcceptance_8()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        String path = "";
        for (String entry : ("1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_8_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[file]", scopes);
    }

    @Test
    void testAcceptance_9() {
        final Generator generator = Generator.parse(("A#[0x0000070000]B").getBytes());
        Assertions.assertEquals("A\00\00\07\00\00B", new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_A()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        for (int loop = 1; loop < 7; loop++) {
            final String charX = Character.toString((char)('A' -1 + loop));
            values.put("case", charX + "1");
            values.put("name", charX + "2");
            values.put("date", charX + "3");
            values.put("size", charX + "4");
            values.put("type", charX + "5");
            values.put("mime", charX + "6");
            generator.set("file", values);
        }
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_A_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_B()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_B_1.txt").getBytes());
        final Map<String, Object> values = new HashMap<>() {{
            put("a", new Hashtable<>() {{
                put("a1", "xa1");
                put("a2", "xa2");
                put("a3", "xa3");
                put("b", new Hashtable<>() {{
                    put("b1", "xb1");
                    put("b2", "xb2");
                    put("b3", "xb3");
                    put("c", new Hashtable<>() {{
                        put("c1", "xc1");
                        put("c2", "xc2");
                        put("c3", "xc3");
                    }});
                }});
            }});
        }};
        generator.set(values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_B_2.txt"), new String(generator.extract()).replaceAll("\\s+", ""));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[a, b, c]", scopes);
    }

    @Test
    void testAcceptance_C()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_C_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>() {{
            put("row", new ArrayList<>() {{
                add(new HashMap<>() {{
                    put("cell", new ArrayList<>() {{
                        add("A1");
                        add("A2");
                        add("A3");
                    }});
                }});
                add(new HashMap<>() {{
                    put("cell", new ArrayList<>() {{
                        add("B1");
                        add("B2");
                        add("B3");
                    }});
                }});
                add(new HashMap<>() {{
                    put("cell", new ArrayList<>() {{
                        add("C1");
                        add("C2");
                    }});
                }});
                add(new HashMap<>() {{
                    put("cell", new ArrayList<>() {{
                        add("D1");
                    }});
                }});
                add(new HashMap<>() {{
                    put("cell", new ArrayList<>() {{
                    }});
                }});
            }});
        }};
        generator.set("table", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testAcceptance_C_2.txt"), new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[row, cell, table]", scopes);
    }

    @Test
    void testAcceptance_D() {
        final String template = "#[0x5065746572]#[0x7c756e64]#[0x7c646572]#[0x7c576f6c66]";
        final Generator generator = Generator.parse(template.getBytes());
        Assertions.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_E() {
        final String template = "#[0x5065746572]#[0x7C756E64]#[0x7C646572]#[0x7C576F6C66]";
        final Generator generator = Generator.parse(template.getBytes());
        Assertions.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_F() {
        final String template = "#[0X5065746572]#[0X7C756E64]#[0X7C646572]#[0X7C576F6C66]";
        final Generator generator = Generator.parse(template.getBytes());
        Assertions.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_G() {
        final String template = "#[x]#[X]";
        final Generator generator = Generator.parse(template.getBytes());
        final Map<String, Object> values = new HashMap<>();
        values.put("X", "1");
        values.put("x", "2");
        generator.set(values);
        Assertions.assertEquals("22", new String(generator.extract()));

        String scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testRecursion_1()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_1_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_2()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_2_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_3()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        values.put("teST", "xx3");
        generator.set("path", values);
        values.put("teST", "xx4");
        generator.set("path", values);
        values.put("teST", "xx5");
        generator.set("path", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_3_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_4()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_2.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        values.put("teST", "xx3");
        generator.set("path", values);
        values.put("teST", "xx4");
        generator.set("path", values);
        values.put("teST", "xx5");
        generator.set("path", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_4_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_5()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_5_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_6()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_6_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_7()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        generator.set("c", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_7_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_8()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        generator.set("c", values);
        generator.set("d", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_8_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_9()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("d", values);
        generator.set("c", values);
        generator.set("b", values);
        generator.set("a", values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_9_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_A()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_0_3.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        values.put("a", values);
        values.put("b", values);
        values.put("c", values);
        values.put("d", values);
        generator.set(values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_A_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_B()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testRecursion_B_1.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("a", "xa");
        values.put("b", "xb");
        values.put("c", "xc");
        generator.set(values);
        Assertions.assertEquals(GeneratorTest.readTestContent("testRecursion_B_2.txt"), new String(generator.extract()));
    }

    @Test
    void testPerformance_1()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_2.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("name", "A");
        values.put("date", "B");
        values.put("size", "C");
        values.put("type", "D");
        values.put("mime", "E");
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        long timing = System.currentTimeMillis();
        for (long loop = 1; loop < 10000; loop++) {
            values.put("case", "X" + loop);
            buffer.write(generator.extract("file", values));
        }
        values.put("file", buffer.toByteArray());
        generator.set("file", values);
        generator.extract();
        final long timeTotal = System.currentTimeMillis() -timing;
        Assertions.assertTrue(timeTotal < 3500, "Expected less than 3500 but was: " + timeTotal);
    }

    @Test
    void testPerformance_2()
            throws Exception {
        final Generator generator = Generator.parse(GeneratorTest.readTestContent("testAcceptance_0_2.txt").getBytes());
        final Hashtable<String, Object> values = new Hashtable<>();
        values.put("name", "A");
        values.put("date", "B");
        values.put("size", "C");
        values.put("type", "D");
        values.put("mime", "E");
        final long timing = System.currentTimeMillis();
        for (long loop = 1; loop < 2500; loop++) {
            values.put("case", "X" + loop);
            generator.set("file", values);
        }
        generator.extract();
        final long timeTotal = System.currentTimeMillis() -timing;
        Assertions.assertTrue(timeTotal < 3500, "Expected less than 3500 but was: " + timeTotal);
    }

    @Test
    void testNullable_1() {
        final Generator generator = Generator.parse(null);
        generator.extract(null);
        generator.extract("");
        generator.extract(null, null);
        generator.extract("", new Hashtable<>());
        generator.set(null);
        generator.set(new Hashtable<>());
        generator.set(null, null);
        generator.set("", new Hashtable<>());
    }
}