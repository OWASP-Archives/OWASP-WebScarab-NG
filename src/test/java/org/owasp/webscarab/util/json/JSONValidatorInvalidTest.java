package org.owasp.webscarab.util.json;

public class JSONValidatorInvalidTest extends JSONTestCase {

    public void testUnknownAlphas() {
        assertInvalid("blah", 1, "value");
        assertInvalid("TRUE",1, "value");
        assertInvalid("True", 1, "value");
        assertInvalid("truee", 5, "end");
        assertInvalid("treu", 1, "literal true");
    }

    public void testMultipleKnownAlphas() {
        assertInvalid("true false", 6, "end");
    }

    public void testMultipleValidNumbers() {
        assertInvalid("123 456", 5, "end");
        assertInvalid("0.1e-43 456", 9, "end");
    }

    public void testCommasOutsideCollection() {
        assertInvalid("true,false", 5, "end");
    }
}
