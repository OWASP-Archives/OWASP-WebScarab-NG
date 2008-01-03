package org.owasp.webscarab.util.json;

public class JSONWriterStringTest extends JSONTestCase {

    public void testSimple() {
        assertEquals("\"hello\"", write("hello"));
    }

    public void testEscapes() {
        assertEquals("\"hel\\tlo\"", write("hel\tlo"));
    }

    public void testUnicode() {
        assertEquals("\"hel\\u0003lo\"", write("hel\u0003lo"));
    }

    public void testChar() {
        assertEquals("\"x\"", write('x'));
    }

}
