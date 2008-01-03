package org.owasp.webscarab.util.json;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JSONTests extends TestCase {
    
    public JSONTests(String name) {
        super(name);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(JSONValidatorValidTest.class);
        suite.addTestSuite(JSONValidatorInvalidTest.class);
        suite.addTestSuite(JSONValidatorInvalidNumberTest.class);
        suite.addTestSuite(JSONValidatorInvalidStringTest.class);
        suite.addTestSuite(JSONValidatorInvalidArrayTest.class);
        suite.addTestSuite(JSONValidatorInvalidObjectTest.class);
        
        suite.addTestSuite(JSONWriterMapTest.class);
        suite.addTestSuite(JSONWriterArrayTest.class);
        suite.addTestSuite(JSONWriterDirectTest.class);
        suite.addTestSuite(JSONWriterStringTest.class);
        suite.addTestSuite(JSONWriterNumberTest.class);

        suite.addTestSuite(JSONReaderOverallTest.class);
        suite.addTestSuite(JSONReaderObjectTest.class);
        suite.addTestSuite(JSONReaderArrayTest.class);
        suite.addTestSuite(JSONReaderDirectTest.class);
        suite.addTestSuite(JSONReaderStringTest.class);
        suite.addTestSuite(JSONReaderNumberTest.class);
        suite.addTestSuite(JSONReaderBlankTest.class);
        
        suite.addTestSuite(JSONReaderObjectRelaxedTest.class);

        return suite;
    }
}
