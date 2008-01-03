package org.owasp.webscarab.util.json;

public class JSONValidatorInvalidStringTest extends JSONTestCase {

    public void testUnterminatedString() {
       assertInvalid("\"abc", 1, "quoted string");
    }

    public void testUnstartedString() {
        assertInvalid("abc\"", 1, "value");
    }

    public void testUnfinishedEscape() {
        assertInvalid("\"abc\\\"", 1, "quoted string");
    }

    public void testUnknownEscape() {
        assertInvalid("\"abc\\x\"", 5, "escape sequence \\\",\\\\,\\/,\\b,\\f,\\n,\\r,\\t or \\uxxxx");
    }

    public void testUnfinishedUnicode() {
        assertInvalid("\"abc\\u\"", 5, "unicode escape sequence \\uxxxx");
        assertInvalid("\"abc\\u1\"", 5, "unicode escape sequence \\uxxxx");
        assertInvalid("\"abc\\u12\"", 5, "unicode escape sequence \\uxxxx");
        assertInvalid("\"abc\\u123\"", 5, "unicode escape sequence \\uxxxx");
    }

    public void testNonHexUnicode() {
        assertInvalid("\"abc\\ux\"", 5, "unicode escape sequence \\uxxxx");
        assertInvalid("\"abc\\u1x\"", 5, "unicode escape sequence \\uxxxx");
        assertInvalid("\"abc\\u12x\"", 5, "unicode escape sequence \\uxxxx");
        assertInvalid("\"abc\\u123x\"", 5, "unicode escape sequence \\uxxxx");
    }

    public void testMultipleStrings() {
        assertInvalid("\"abc\"\"def\"", 6, "end");
    }

    public void testUnescapedQuote() {
        assertInvalid("\"abc\"def\"", 6, "end");
    }
}
