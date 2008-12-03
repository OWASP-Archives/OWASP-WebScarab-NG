package org.owasp.webscarab.util.json;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class JSONWriterArrayTest extends JSONTestCase {

    public void testEmpty() {
        List list = new ArrayList();
        assertEquals("[]", write(list));
    }

	public void testSingle() {
        List list = new ArrayList();
        list.add("hello");
        assertEquals("[\"hello\"]", write(list));
    }

    public void testSequence() {
        List list = new ArrayList();
        list.add("hello");
        list.add("there");
        assertEquals("[\"hello\",\"there\"]", write(list));
    }

    public void testNested() {
        List list = new ArrayList();
        list.add("hello");
        List list2 = new ArrayList();
        list2.add("123");
        list2.add("456");
        list.add(list2);
        assertEquals("[\"hello\",[\"123\",\"456\"]]", 
                write(list));
    }

    public void testList() {
        List list = new ArrayList();
        list.add("hello");
        list.add(new Integer(123));
        assertEquals("[\"hello\",123]", write(list));
    }

}
