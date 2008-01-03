package org.owasp.webscarab.util.json;

public class JSONWriterDirectTest extends JSONTestCase {
    
    public void testBoolean() {
        assertEquals("true", write(true));
        assertEquals("false", write(false));
    }

    public void testNull() {
        assertEquals("null", write(null));
    }
}
