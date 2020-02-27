package com.seanox.pdf;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** 
 * Wrapper to run the {@link Preview} with the test classes and resources.
 * Please remove {@link Disabled} for designing.
 */
public class PreviewTest {

    @Test
    public void testMain() {
        Preview.main(new String[] {"./src/test/resources/pdf/*.html", "*.html"});
    }
}