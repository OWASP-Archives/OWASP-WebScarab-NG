package org.owasp.webscarab.util.json;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JSONReaderObjectRelaxedTest extends JSONTestCase {

    Map map;

    private Map get(String string) {
        try {
            try {
                obj = new JSONReader(string).parse(JSONReader.STRICT);
                fail("Expected compliance exception!");
            } catch (JSONComplianceException jce) {
                // expected
            }
            obj = new JSONReader(string).parse(JSONReader.JS_EVAL);
        } catch (ParseException pe) {
            System.err.println("========\nUnexpected exception parsing '" + string + "'");
            pe.printStackTrace();
            fail("Unexpected exception : " + pe);
        }
        map = map(obj);
        return map;
    }

    private Map map(Object obj) {
        assertTrue(obj instanceof Map);
        return (Map) obj;
    }

    public void testSingleItem() {
        Map map = get("{first:123}");
        assertEquals(1, map.size());
        assertInteger(123, map.get("first"));
    }

    public void testNestedArray() {
        assertEquals(1, get("{first:[123,345]}").size());
        Object obj = map.get("first");
        assertTrue(obj instanceof List);
        List col = (List)obj;
        assertEquals(2, col.size());
        assertEquals(123, ((Number)col.get(0)).intValue());
        assertEquals(345, ((Number)col.get(1)).intValue());
    }

    public void testNestedEmptyArray() {
        assertEquals(1, get("{first:[]}").size());
        Object obj = map.get("first");
        assertTrue(obj instanceof List);
        List col = (List)obj;
        assertEquals(0, col.size());
    }

    public void testTwoItems() {
        assertEquals(2, get("{first:123,second:\"456x\"}").size());
        assertInteger(123, map.get("first"));
        assertEquals("456x", map.get("second"));
    }

    public void testSingleItemithSpaces() {
        assertEquals(1, get("{  first  :  123  }").size());
        assertInteger(123, map.get("first"));
    }

    public void testTwoItemsWithSpaces() {
        assertEquals(2, get(
                "{  first  :  123  ,  second  :  \"456x\"  }").size());
        assertInteger(123, map.get("first"));
        assertEquals("456x", map.get("second"));
    }

    public void testNestedMaps() {
        assertEquals(
                2,
                get(
                        "{  first  :  { abc:\"def\", 123:\"789x\" } ,  second  :  \"456x\"  }")
                        .size());
        Map first = map(map.get("first"));
        assertEquals("def", first.get("abc"));
        assertEquals("789x", first.get(Long.valueOf(123)));
        assertEquals("456x", map.get("second"));
    }
}
