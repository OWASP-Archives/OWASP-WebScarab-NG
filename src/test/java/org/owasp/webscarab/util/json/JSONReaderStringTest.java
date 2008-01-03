package org.owasp.webscarab.util.json;

public class JSONReaderStringTest extends JSONTestCase {

    public void testEmptyString() {
        assertEquals("", read("\"\""));
    }

    public void testBlankString() {
        assertEquals(" ", read("\" \""));
    }

    public void testletters() {
        assertEquals("abc", read("\"abc\""));
    }

    public void testEscapes() {
        assertEquals("ab\"c", read("\"ab\\\"c\""));
        assertEquals("ab\\c", read("\"ab\\\\c\""));
        assertEquals("ab/c", read("\"ab\\/c\""));
        assertEquals("ab\bc", read("\"ab\\bc\""));
        assertEquals("ab\fc", read("\"ab\\fc\""));
        assertEquals("ab\nc", read("\"ab\\nc\""));
        assertEquals("ab\rc", read("\"ab\\rc\""));
        assertEquals("ab\tc", read("\"ab\\tc\""));
    }

    public void testUnicode() {
        assertEquals("ab\u1234c", read("\"ab\\u1234c\""));
        assertEquals("ab\uabcdc", read("\"ab\\uabcdc\""));
        assertEquals("ab\uCDEFc", read("\"ab\\uCDEFc\""));
    }
}
