package org.owasp.webscarab.util.json;

public class JSONValidatorInvalidArrayTest extends JSONTestCase {

    public void testSingleOpen() {
        assertInvalid("[", 2, "value");
    }

    public void testSingleOpenAndSpace() {
        assertInvalid("[ ", 2, "value");
    }

    public void testDoubleOpen() {
        assertInvalid("[[", 3, "value");
    }

    public void testSingleClose() {
        assertInvalid("]", 1, "value");
    }

    public void testNonValue() {
        assertInvalid("[a]", 2, "value");
    }

    public void testMissingComma() {
        assertInvalid("[true 1]", 7, "comma or ]");
    }

    public void testleadingComma() {
        assertInvalid("[,3]", 2, "value");
    }

    public void testTrailingComma() {
        assertInvalid("[5,]", 4, "value");
    }

    public void testDoubleComma() {
        assertInvalid("[5,,6]", 4, "value");
    }
}
