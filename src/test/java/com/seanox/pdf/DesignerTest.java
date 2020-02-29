/**
 * LIZENZBEDINGUNGEN - Seanox Software Solutions ist ein Open-Source-Projekt, im
 * Folgenden Seanox Software Solutions oder kurz Seanox genannt.
 *  Diese Software unterliegt der Version 2 der Apache License.
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** 
 * Wrapper to run the {@link Designer} with the test classes and resources.
 * Test are ignored because the designer is a service and runs endlessly.
 * Please remove {@link Disabled} for designing.<br>
 * <br>
 * DesignerTest 3.2.0 20200229<br>
 * Copyright (C) 2020 Seanox Software Solutions<br>
 * Alle Rechte vorbehalten.
 *
 * @author  Seanox Software Solutions
 * @version 3.2.0 20200229
 */
public class DesignerTest {

    @Test
    @Disabled
    public void testMain() throws Exception {
        Designer.main(new String[] {"./src/test/resources/pdf/*.html", "*.html"});
    }
}