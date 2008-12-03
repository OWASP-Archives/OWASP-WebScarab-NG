package org.owasp.webscarab.util.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JSONWriterMapTest extends JSONTestCase {
    
    Map map = new HashMap();

    private void assertMap(String expected) {
        assertEquals(expected, write(map));
    }

    public void testEmpty() {
        assertMap("{}");
    }

    public void testSimple() {
        map.put("hello", "world");
        assertMap("{\"hello\":\"world\"}");
    }

    public void testMultiple() {
        map.put("hello", "world");
        map.put("goodbye", "cruel");
        assertMap("{\"hello\":\"world\",\"goodbye\":\"cruel\"}");
    }

    public void testMixed() {
        map.put("hello", "world");
        map.put("goodbye", new Integer(123));
        assertMap("{\"hello\":\"world\",\"goodbye\":123}");
    }

    public void testNestedMap() {
        Map child = new HashMap();
        child.put("whetever", "abc");
        child.put("X Y", Boolean.FALSE);
        map.put("hello", "world");
        map.put("goodbye", child);
        assertMap("{\"hello\":\"world\",\"goodbye\":{\"X Y\":false,\"whetever\":\"abc\"}}");
    }

    public void testNestedArray() {
        List child = new ArrayList();
        child.add(123);
        child.add(456);
        map.put("hello", "world");
        map.put("goodbye", child);
        assertMap("{\"hello\":\"world\",\"goodbye\":[123,456]}");
    }

    public void testNestedEmptyArray() {
        List child = new ArrayList();
        map.put("hello", "world");
        map.put("goodbye", child);
        assertMap("{\"hello\":\"world\",\"goodbye\":[]}");
    }

    public void testSelfReferentialObject() {
        List list = new ArrayList();
        list.add(list);
        assertEquals("[null]", write(list));
    }
    
    public void testMapCyclicReference() {
        Map map1 = new HashMap();
        Map map2 = new HashMap();
        Map map3 = new HashMap();
        
        map1.put("link", map2);
        map2.put("link", map3);
        map3.put("link", map1);

        assertEquals("{\"link\":{\"link\":{\"link\":null}}}", write(map1));
    }
    
    public void testArrayCyclicReference() {
        Collection c1 = new ArrayList();
        Collection c2 = new ArrayList();
        Collection c3 = new ArrayList();
        
        c1.add("c1");
        c1.add(c2);
        c2.add("c2");
        c2.add(c3);
        c3.add("c3");
        c3.add(c1);

        assertEquals("[\"c1\",[\"c2\",[\"c3\",null]]]", write(c1));
    }
}
