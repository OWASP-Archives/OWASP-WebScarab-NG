package org.owasp.webscarab.util.json;

import java.util.List;

@SuppressWarnings("unchecked")
public class JSONReaderDirectTest extends JSONTestCase {

    public void testAll() {
        assertEquals(Boolean.TRUE, read("true"));
        assertEquals(Boolean.FALSE, read("false"));
        assertEquals(null,read("null"));
    }

    public void testEmbedded() {
        List list = (List)read("[true,false,null]");
        assertEquals(3, list.size());
        assertEquals(Boolean.TRUE, list.get(0));
        assertEquals(Boolean.FALSE, list.get(1));
        assertEquals(null, list.get(2));
    }
}
