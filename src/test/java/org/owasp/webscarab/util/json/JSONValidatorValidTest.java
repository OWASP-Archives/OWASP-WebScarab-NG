package org.owasp.webscarab.util.json;

public class JSONValidatorValidTest extends JSONTestCase {

    public void testEmpty() {
        assertValid("");
    }
    
    public void testLiteral() {
        assertValid("true");
        assertValid("false");
        assertValid("null");
        assertValid(" true");
        assertValid("true ");
        assertValid(" true ");
    }

    public void testString() {
        assertValid("\"\"");
        assertValid("\"abc\"");
        assertValid("\"123\"");
        assertValid("\"true\"");
        assertValid("\"\\\"\"");
        assertValid("\"\\\\\"");
        assertValid("\"\\/\"");
        assertValid("\"\\b\"");
        assertValid("\"\\f\"");
        assertValid("\"\\n\"");
        assertValid("\"\\r\"");
        assertValid("\"\\t\"");
        assertValid("\"\\u123a\"");
        assertValid("\"\\ua123\"");
        assertValid("\"\\u12345\"");
        assertValid(" \"abc\"");
        assertValid("\"abc\" ");
        assertValid(" \"abc\" ");
    }

    public void testNumber() {
        assertValid("0");
        assertValid("1");
        assertValid("-1");
        assertValid("123");
        assertValid(" 123");
        assertValid("123 ");
        assertValid(" 123 ");
        assertValid("123.1");
        assertValid("-123.1");
        assertValid("0.01");
        assertValid("-0.01");
        assertValid("1e4");
        assertValid(" 1e4");
        assertValid("1e4 ");
        assertValid(" 1e4 ");
        assertValid("12E4");
        assertValid("-12E4");
        assertValid("12E+4");
        assertValid("-12E+4");
        assertValid("12E-4");
        assertValid("-12E-4");
        assertValid("0.01e-4");
        assertValid("-0.01e-29");
    }
    
    public void testArray() {
        assertValid("[]");
        assertValid("[ ]");
        assertValid("[ ] ");
        assertValid("[true]");
        assertValid("[ true ]");
        assertValid("[\"abc\"]");
        assertValid("[\"a]c\"]");
        assertValid("[ \"abc\" ]");
        assertValid("[123]");
        assertValid("[ 0.1e-5 ]");
        assertValid("[true,false]");
        assertValid("[true ,false]");
        assertValid("[true, false]");
        assertValid("[true , false]");
        assertValid("[[true]]");
        assertValid("[false,[true]]");
        assertValid("[[true],false]");
        assertValid("[[true],[false]]");
        assertValid("[ [ true ] , [ false ] ]");
    }
    
    public void testObject() {
        assertValid("{}");
        assertValid("{ }");
        assertValid("{ } ");
        assertValid("{\"a\":true}");
        assertValid("{\"a\":false}");
        assertValid("{\"a\":null}");
        assertValid("{\"a\":123}");
        assertValid("{\"a\":-0.1e-4}");
        assertValid("{\"a\":\"hello\"}");
        assertValid("{\"a\":[true]}");
        assertValid("{\"a\":{\"b\":true}}");
        assertValid("{\"a\":true,\"b\":false}");
        assertValid("{\"a\":true,\"b\":false,\"ccc\":123}");
        assertValid("{ \"a\":true}");
        assertValid("{\"a\" :true}");
        assertValid("{\"a\": true}");
        assertValid("{\"a\":true }");
        assertValid("{ \"a\":true}");
        assertValid("{ \"a\" :true}");
        assertValid("{ \"a\": true}");
        assertValid("{ \"a\":true }");
        assertValid("{ \"a\" : true}");
        assertValid("{ \"a\" :true }");
        assertValid("{ \"a\" : true }");
        assertValid("{\"a\":true,\"b\":false}");
        assertValid("{\"a\":true ,\"b\":false}");
        assertValid("{\"a\":true, \"b\":false}");
        assertValid("{\"a\":true,\"b\":false }");
    }
}
