package org.owasp.webscarab.util.json;

import java.math.BigDecimal;

public class JSONWriterNumberTest extends JSONTestCase {

    public void testInteger() {
        assertEquals("123", write(123));
    }

    public void testFloat() {
        assertEquals("123.456", write(123.456));
    }

    public void testExp() {
        assertEquals("1.23456E14", write(123.456e12));
        assertEquals("1.23456E-10", write(123.456e-12));
    }

    public void testBig() {
        assertEquals("123", write(new BigDecimal(123)));
    }
}
