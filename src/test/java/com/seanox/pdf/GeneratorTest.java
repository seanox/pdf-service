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

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Hashtable;

/**
 * Unit test for Generator.
 *
 * @author  Seanox Software Solutions
 * @version 4.2.0 20220806
 */
class GeneratorTest {

    @Test
    void testAcceptance_1()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_1_1.txt").getBytes());
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_1_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_2()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_2_1.txt").getBytes());
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_2_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_3()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_1.txt").getBytes());
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_3_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_4()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        var path = "";
        for (final var entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_4_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_5()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        final var buffer = new ByteArrayOutputStream();
        for (var loop = 1; loop < 7; loop++) {
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
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_5_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_6()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_2.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        var path = "";
        for (final var entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_6_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[file]", scopes);
    }

    @Test
    void testAcceptance_7()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        var path = "";
        for (final var entry : ("/1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_7_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[file]", scopes);
    }

    @Test
    void testAcceptance_8()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        var path = "";
        for (final var entry : ("1/22/333/4444/55555").split("/")) {
            path = path.concat(entry);
            values.put("base", path);
            values.put("name", entry);
            generator.set("path", values);
        }
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_8_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[file]", scopes);
    }

    @Test
    void testAcceptance_9() {
        final var generator = Generator.parse(("A#[0x0000070000]B").getBytes());
        Assertions.assertEquals("A\00\00\07\00\00B", new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_A()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        for (var loop = 1; loop < 7; loop++) {
            final String charX = Character.toString((char)('A' -1 + loop));
            values.put("case", charX + "1");
            values.put("name", charX + "2");
            values.put("date", charX + "3");
            values.put("size", charX + "4");
            values.put("type", charX + "5");
            values.put("mime", charX + "6");
            generator.set("file", values);
        }
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_A_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[path, file]", scopes);
    }

    @Test
    void testAcceptance_B()
            throws Exception {
        final var values = TestDataReader.readTestDataMap("testAcceptance_B.json");
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_B_1.txt").getBytes());
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_B_2.txt"), new String(generator.extract()).replaceAll("\\s+", ""));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[a, b, c]", scopes);
    }

    @Test
    void testAcceptance_C()
            throws Exception {
        final var values = TestDataReader.readTestDataMap("testAcceptance_C.json");
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_C_1.txt").getBytes());
        generator.set("table", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_C_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[row, cell, table]", scopes);
    }

    @Test
    void testAcceptance_D() {
        final var template = "#[0x5065746572]#[0x7c756e64]#[0x7c646572]#[0x7c576f6c66]";
        final var generator = Generator.parse(template.getBytes());
        Assertions.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_E() {
        final var template = "#[0x5065746572]#[0x7C756E64]#[0x7C646572]#[0x7C576F6C66]";
        final var generator = Generator.parse(template.getBytes());
        Assertions.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_F() {
        final var template = "#[0X5065746572]#[0X7C756E64]#[0X7C646572]#[0X7C576F6C66]";
        final var generator = Generator.parse(template.getBytes());
        Assertions.assertEquals("Peter|und|der|Wolf", new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_G()
            throws Exception {
        final var template = "#[x]#[X]";
        final var values = TestDataReader.readTestDataMap("testAcceptance_G.json");
        final var generator = Generator.parse(template.getBytes());
        generator.set(values);
        Assertions.assertEquals("22", new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[]", scopes);
    }

    @Test
    void testAcceptance_H()
            throws Exception {
        final var values = TestDataReader.readTestDataMap("testAcceptance_H.json");
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_H_1.txt").getBytes());
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_H_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[a, b]", scopes);
    }

    @Test
    void testAcceptance_I()
            throws Exception {
        final var values = TestDataReader.readTestDataMap("testAcceptance_I.json");
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_I_1.txt").getBytes());
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_I_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[price, articles]", scopes);
    }

    @Test
    void testAcceptance_J()
            throws Exception {
        final var values = TestDataReader.readTestDataMap("testAcceptance_J.json");
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_J_1.txt").getBytes());
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_J_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[$, _articles]", scopes);
    }

    @Test
    void testAcceptance_K()
            throws Exception {
        final var values = TestDataReader.readTestDataMap("testAcceptance_K.json");
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_K_1.txt").getBytes());
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testAcceptance_K_2.txt"), new String(generator.extract()));

        final var scopes = Collections.list(generator.scopes()).toString();
        Assertions.assertEquals("[a]", scopes);
    }

    @Test
    void testRecursion_1()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("teST", "xx1");
        generator.set("path", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_1_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_2()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("teST", "xx1");
        generator.set("path", values);
        values.put("teST", "xx2");
        generator.set("path", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_2_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_3()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
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
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_3_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_4()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_2.txt").getBytes());
        final var values = new Hashtable<String, Object>();
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
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_4_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_5()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_5_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_6()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_6_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_7()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        generator.set("c", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_7_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_8()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("a", values);
        generator.set("b", values);
        generator.set("c", values);
        generator.set("d", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_8_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_9()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        generator.set("d", values);
        generator.set("c", values);
        generator.set("b", values);
        generator.set("a", values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_9_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_A()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_0_3.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("bv", "bv-ok");
        values.put("cv", "cv-ok");
        values.put("dv", "dv-ok");
        values.put("b1v", "b1v-ok");
        values.put("a", values);
        values.put("b", values);
        values.put("c", values);
        values.put("d", values);
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_A_2.txt"), new String(generator.extract()));
    }

    @Test
    void testRecursion_B()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testRecursion_B_1.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("a", "xa");
        values.put("b", "xb");
        values.put("c", "xc");
        generator.set(values);
        Assertions.assertEquals(TestDataReader.readTestContent("testRecursion_B_2.txt"), new String(generator.extract()));
    }

    @Test
    void testPerformance_1()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_2.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("name", "A");
        values.put("date", "B");
        values.put("size", "C");
        values.put("type", "D");
        values.put("mime", "E");
        final var buffer = new ByteArrayOutputStream();
        final var timing = System.currentTimeMillis();
        for (var loop = 1; loop < 10000; loop++) {
            values.put("case", "X" + loop);
            buffer.write(generator.extract("file", values));
        }
        values.put("file", buffer.toByteArray());
        generator.set("file", values);
        generator.extract();
        final var timeTotal = System.currentTimeMillis() -timing;
        Assertions.assertTrue(timeTotal < 3500, "Expected less than 3500 but was: " + timeTotal);
    }

    @Test
    void testPerformance_2()
            throws Exception {
        final var generator = Generator.parse(TestDataReader.readTestContent("testAcceptance_0_2.txt").getBytes());
        final var values = new Hashtable<String, Object>();
        values.put("name", "A");
        values.put("date", "B");
        values.put("size", "C");
        values.put("type", "D");
        values.put("mime", "E");
        final var timing = System.currentTimeMillis();
        for (var loop = 1; loop < 2500; loop++) {
            values.put("case", "X" + loop);
            generator.set("file", values);
        }
        generator.extract();
        final var timeTotal = System.currentTimeMillis() -timing;
        Assertions.assertTrue(timeTotal < 2500, "Expected less than 2500 but was: " + timeTotal);
    }

    @Test
    void testNullable_1() {
        final var generator = Generator.parse(null);
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