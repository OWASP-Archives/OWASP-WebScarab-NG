package org.owasp.webscarab.util.json;

public class JSONValidatorInvalidNumberTest extends JSONTestCase {

    public void testMultipleZero() {
        assertInvalid("00", 2, "end");
    }

    public void testleadingZero() {
        assertInvalid("01", 2, "end");
    }
    
    public void testLeadingPlus() {
        assertInvalid("+1", 1, "value");
    }
    
    public void testMinusLeadingZero() {
        assertInvalid("-01", 3, "end");
    }
    
    public void testMinusMultipleZero() {
        assertInvalid("-00", 3, "end");
    }
    
    public void testMultipleMinus() {
        assertInvalid("--1", 1, "number");
    }
    
    public void testSoloMinus() {
        assertInvalid("-", 1, "number");
    }
    
    public void testTrailingPoint() {
        assertInvalid("12.", 1, "number");
    }
    
    public void testDoublePoint() {
        assertInvalid("12..2", 1, "number");
    }
    
    public void testTrailingE() {
        assertInvalid("12e", 1, "number");
    }
    
    public void testFollowingAlpha() {
        assertInvalid("12b", 3, "end");
    }
}
