package org.owasp.webscarab.util.json;

import java.text.ParseException;
import java.util.List;

@SuppressWarnings("unchecked")
public class JSONReaderArrayTest extends JSONTestCase {

    List array;

    private List get(String string) throws ParseException {
        array = array(read(string));
        return array;
    }

    private List array(Object obj) {
        assertTrue(obj instanceof List);
        return (List) obj;
    }

    public void testEmptyArray()  throws ParseException {
        assertEquals(0, get("[]").size());
    }

    public void testSingleNumber() throws ParseException {
        assertEquals(1, get("[123]").size());
        assertInteger(123, array.get(0));
    }

    public void testSingleNumberWithSpaces() throws ParseException {
        assertEquals(1, get("[ 123 ]").size());
        assertInteger(123, array.get(0));
    }

    public void testSingleString() throws ParseException {
        assertEquals(1, get("[\"123x\"]").size());
        assertEquals("123x", array.get(0));
    }

    public void testTwoNumbers() throws ParseException {
        assertEquals(2, get("[123,456]").size());
        assertInteger(123, array.get(0));
        assertInteger(456, array.get(1));
    }

    public void testTwoStrings() throws ParseException {
        assertEquals(2, get("[\"123x\",\"456y\"]").size());
        assertEquals("123x", array.get(0));
        assertEquals("456y", array.get(1));
    }

    public void testTwoNumbersWithSpaces() throws ParseException {
        assertEquals(2, get("[ 123 , 456 ]").size());
        assertInteger(123, array.get(0));
        assertInteger(456, array.get(1));
    }

    public void testNestedArray() throws ParseException {
        assertEquals(2, get("[123,[\"hello\",17.52]]").size());
        assertInteger(123, array.get(0));
        List child = array(array.get(1));
        assertEquals(2, child.size());
        assertEquals("hello", child.get(0));
        assertDouble(17.52, child.get(1));
    }
}
