package com.seanox.pdf;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** 
 * Wrapper to run the {@link Designer} with the test classes and resources.
 * Test are ignored because the designer is a service and runs endlessly.
 * Please remove {@link Disabled} for designing.
 */
public class DesignerTest {

    @Test
    @Disabled
    public void testMain() throws Exception {
        Designer.main(new String[] {"./src/test/resources/pdf/*.html", "*.html"});
    }
}