package org.owasp.webscarab.util.json;

public class JSONReaderBlankTest extends JSONTestCase {

    public void testEmpty() {
        assertEquals(null, read(""));
    }

    public void testJustWhiteSpace() {
        assertEquals(null, read(" "));
        assertEquals(null, read("  "));
        assertEquals(null, read("\t  "));
        assertEquals(null, read("  \t"));
        assertEquals(null, read("\n  "));
        assertEquals(null, read("  \n"));
        assertEquals(null, read("\f  "));
        assertEquals(null, read("  \f"));
    }

    public void testLotsOfMixedWhiteSpace() {
        assertEquals(null, read(" \n \t \f \n \r\n \t\t \n\r"));
        assertInteger(123, read(" \n \t \f \n \r\n \t\t \n\r123"));
    }

}
