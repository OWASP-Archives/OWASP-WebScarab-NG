package org.owasp.webscarab.util.json;

public class JSONValidatorInvalidObjectTest extends JSONTestCase {

    public void testSingleOpen() {
        assertInvalid("{", 2, "string");
    }

    public void testSingleOpenAndSpace() {
        assertInvalid("{ ", 2, "string");
    }

    public void testDoubleOpen() {
        assertInvalid("{{", 2, "string");
    }

    public void testSingleClose() {
        assertInvalid("}", 1, "value");
    }

    public void testNonJustKey() {
        assertInvalid("{\"a\"}", 5, "colon");
    }

    public void testNoValue() {
        assertInvalid("{\"a\":}", 6, "value");
    }

    public void testNonStringKey() {
        assertInvalid("{a}", 2, "string");
    }

    public void testMissingColon() {
        assertInvalid("{\"a\" true}", 6, "colon");
    }

    public void testMissingComma() {
        assertInvalid("{\"a\":true \"b\":false}", 11, "comma or }");
    }

    public void testLeadingComma() {
        assertInvalid("{,\"b\":false}", 2, "string");
    }

    public void testTrailingComma() {
        assertInvalid("{\"a\":true,}", 11, "string");
    }

    public void testDoubleColon() {
        assertInvalid("{\"a\"::true,}", 6, "value");
    }

    public void testDoubleComma() {
        assertInvalid("{\"a\":true,,\"b\":false}", 11, "string");
    }
}
