package org.owasp.webscarab.util.json;

public class JSONReaderNumberTest extends JSONTestCase {

    public void testZero() {
        assertInteger(0, read("0"));
    }

    public void testSingleDigit() {
        assertInteger(1, read("1"));
    }

    public void testLotsOfDigits() {
        assertInteger(123987, read("123987"));
    }

    public void testDigitsAndPoint() {
        assertDouble(123.987, read("123.987"));
    }

    public void testTrailingZeros() {
        assertDouble(123.000, read("123.000"));
    }

    public void testNegative() {
        assertInteger(-123987, read("-123987"));
    }

    public void testNegativeFloat() {
        assertDouble(-123.987, read("-123.987"));
    }

    public void testLittleE() {
        assertDouble(-123.987e22, read("-123.987e22"));
    }

    public void testBigE() {
        assertDouble(-123.987e22, read("-123.987E22"));
    }

    public void testLittleEPlus() {
        assertDouble(-123.987e22, read("-123.987e+22"));
    }

    public void testBigEPlus() {
        assertDouble(-123.987e22, read("-123.987E+22"));
    }

    public void testLittleEMinus() {
        assertDouble(-123.987e-22,read("-123.987e-22"));
    }

    public void testBigEMinus() {
        assertDouble(-123.987e-22, read("-123.987E-22"));
    }

    public void testLotsAndLotsOfDigits() {
        String s = "1234567890123456789";
        assertEquals(s, read(s).toString());
    }
}
