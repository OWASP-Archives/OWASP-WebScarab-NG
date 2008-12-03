package org.owasp.webscarab.util.json;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;

import junit.framework.TestCase;

public abstract class JSONTestCase extends TestCase {
    JSONReader reader;
    Object obj;
    
    protected Object read(String text) {
        try {
            obj = new JSONReader(text).parse();
            return obj;
        } catch (ParseException pe) {
            System.err.println("=========\nGot unexpected exception parsing '" + text + "'");
            pe.printStackTrace();
            fail("ParseException unexpected: " + pe);
            return null;
        }
    }
    
//    protected String write(Object obj) {
//        return writer.write(obj);
//    }
//    
//    protected String write(boolean bool) {
//        return writer.write(bool);
//    }
//
//    protected String write(long n) {
//        return writer.write(n);
//    }
//
//    protected Object write(double d) {
//        return writer.write(d);
//    }
//
//    protected String write(char c) {
//        return writer.write(c);
//    }
//
    protected void assertInteger(int desired, Object value) {
        assertTrue(value instanceof Long);
        assertEquals(desired, ((Number) value).intValue());
    }

    protected void assertDouble(double desired, Object value, double tolerance) {
        assertTrue(value instanceof Double);
        assertEquals(desired, ((Number) value).doubleValue(), tolerance);
    }

    protected void assertDouble(double desired, Object value) {
        assertDouble(desired, value, 0.0001);
    }

    protected void assertInvalid(String text, int col, String message) {
        try {
            new JSONReader(text).parse();
            fail("Exception expected");
        } catch (ParseException pe) {
            // expected
//            System.err.println("======\nExpected '" + message + "', got: \n");
//            pe.printStackTrace();
        }
    }
    
    protected void assertValid(String text) {
        try {
            new JSONReader(text).parse();
        } catch (ParseException pe) {
            fail("No exception expected for '" + text + "'. Got " + pe);
        }
    }
    
    protected String write(Object obj) {
        try {
            StringWriter sw = new StringWriter();
            JSONWriter writer = new JSONWriter(sw, JSONWriter.STRICT);
            writer.write(obj);
            return sw.getBuffer().toString();
        } catch (IOException ioe) {
            fail("Unexpected exception: " + ioe);
            return null;
        }
    }
}
