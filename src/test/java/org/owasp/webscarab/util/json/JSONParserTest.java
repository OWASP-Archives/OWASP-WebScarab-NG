/**
 * 
 */
package org.owasp.webscarab.util.json;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author rdawes
 *
 */
@SuppressWarnings("unchecked")
public class JSONParserTest extends TestCase {

    /**
     * Test method for {@link org.owasp.webscarab.util.json.JSONReader#parse()}.
     */
    public void testParseStrings() {
        try {
            String json = "\"abcdef ghijkl\"";
            assertEquals(json, "\"" + new JSONReader(json).parse() + "\"");
        } catch (Exception e) {
            fail("No exception expected here");
        }
        try {
            String json = "\"abcdef ghijkl";
            new JSONReader(json).parse();
            fail("Exception expected");
        } catch (ParseException pe) {
            assertEquals("End of stream reached", pe.getCause().getMessage());
        }
    }

    public void testParseNumbers() {
        try {
            String json = "12345";
            assertEquals(json, new JSONReader(json).parse().toString());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            String json = "00.00";
            new JSONReader(json).parse();
            fail("Exception expected");
        } catch (ParseException pe) {
            assertEquals("Error parsing a number", pe.getMessage());
        }
        try {
            String json = "0.0123";
            assertEquals(json, new JSONReader(json).parse().toString());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        
    }
    
    public void testParseObjects() {
        try {
            String json = "  { \"a\" : \"A\" } ";
            Map<Object, Object> object = (Map<Object, Object>) new JSONReader(json).parse();
            assertEquals("A", object.get("a"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
        try {
            String json = "  { \"a\" : \"A\", \"b\" : \"B\" } ";
            Map<Object, Object> object = (Map<Object, Object>) new JSONReader(json).parse();
            assertEquals("A", object.get("a"));
            assertEquals("B", object.get("b"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }
    
    public void testParseArrays() {
        try {
            String json = "  [ \"a\", \"b\" ] ";
            List<Object> list = (List<Object>) new JSONReader(json).parse();
            assertEquals("a", list.get(0));
            assertEquals("b", list.get(1));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }
    
    public void testParseReservedWords() {
        try {
            String json = "  [ null, true, false ] ";
            List<Object> list = (List<Object>) new JSONReader(json).parse();
            assertEquals(null, list.get(0));
            assertEquals(Boolean.TRUE, list.get(1));
            assertEquals(Boolean.FALSE, list.get(2));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
        try {
            String json = "  { \"a\" : true, \"b\" : false } ";
            Map<Object, Object> object = (Map<Object, Object>) new JSONReader(json).parse();
            assertEquals(Boolean.TRUE, object.get("a"));
            assertEquals(Boolean.FALSE, object.get("b"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
        try {
            String json = "  { false : null, \"b\" : false } ";
            Map<Object, Object> object = (Map<Object, Object>) new JSONReader(json).parse(JSONReader.STRICT);
            fail("Expected an exception, got " + object);
        } catch (Exception e) {
            assertTrue(e instanceof JSONComplianceException);
        }
        try {
            String json = "  { false : null, \"b\" : false } ";
            Map<Object, Object> object = (Map<Object, Object>) new JSONReader(json).parse(JSONReader.JS_EVAL);
            assertEquals(null, object.get("false"));
            assertEquals(Boolean.FALSE, object.get("b"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e);
        }
    }
}
