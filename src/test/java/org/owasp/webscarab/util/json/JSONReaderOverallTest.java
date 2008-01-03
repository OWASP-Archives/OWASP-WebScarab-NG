package org.owasp.webscarab.util.json;

import java.util.List;

import org.stringtree.json.JSONReader;

public class JSONReaderOverallTest extends JSONTestCase {

    List list;
    
    public void testSimple() {
        read("123");
        assertTrue(obj instanceof Number);
        assertEquals(123, ((Number) obj).intValue());
    }

    public void testSequence() {
        read("[ \"world\", \"hello\" ]");
        assertTrue(obj instanceof List);
        list = ((List) obj);
        assertEquals("world", list.get(0));
        assertEquals("hello", list.get(1));
        
        read("[ \"hello\", \"world\" ]");
        list = ((List) obj);
        assertEquals("hello", list.get(0));
        assertEquals("world", list.get(1));
    }

    public void testParallel() {
        JSONReader reader1 = new JSONReader();
        JSONReader reader2 = new JSONReader();
        obj = reader2.read("[ \"world\", \"hello\" ]");
        assertTrue(obj instanceof List);
        list = ((List) obj);
        assertEquals("world", list.get(0));
        assertEquals("hello", list.get(1));
        obj = reader1.read("[ \"hello\", \"world\" ]");
        list = ((List) obj);
        assertEquals("hello", list.get(0));
        assertEquals("world", list.get(1));
    }
}
