/**
 * 
 */
package org.owasp.webscarab.util.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rdawes
 *
 */
public class JSONReader {

    private static final Map<Character, Character> escapes = new HashMap<Character, Character>();
    static {
        escapes.put(new Character('"'), new Character('"'));
        escapes.put(new Character('\\'), new Character('\\'));
        escapes.put(new Character('/'), new Character('/'));
        escapes.put(new Character('b'), new Character('\b'));
        escapes.put(new Character('f'), new Character('\f'));
        escapes.put(new Character('n'), new Character('\n'));
        escapes.put(new Character('r'), new Character('\r'));
        escapes.put(new Character('t'), new Character('\t'));
    }

    public static final int STRICT = 1;
    
    public static final int JS_EVAL = 2;
    
    private CharacterIterator text;
    
    private int compliance;
    
    private StringBuilder buff = new StringBuilder();
    
    private boolean end = false;
    
    public JSONReader(String text) {
        this(new StringCharacterIterator(text));
    }
    
    public JSONReader(CharacterIterator text) {
        this.text = text;
    }
    
    public Object parse() throws ParseException {
        return parse(STRICT);
    }
    
    public Object parse(int compliance) throws ParseException {
        this.compliance = compliance;
        end = false;
        text.first();
        Object value = parseValue();
        consumeWhiteSpace();
        if (text.current() == CharacterIterator.DONE)
            return value;
        throw new ParseException("Unexpected character '" + text.current() + "', expected the end of the JSON object", text.getIndex());
    }
    
    private Object parseValue() throws ParseException {
        consumeWhiteSpace();
        char ch = text.current();
        if (ch == '{')
            return parseObject();
        if (ch == '[')
            return parseArray();
        if (Character.isDigit(ch) || ch == '-')
            return parseNumber();
        if (ch == '"')
            return parseString();
        if (ch == 't' || ch == 'f' || ch == 'n')
            return parseReservedWord();
        if (ch == CharacterIterator.DONE)
            return null;
        throw new ParseException("Unexpected character '" + ch + "'", text.getIndex());
    }
    
    private Map<Object, Object> parseObject() throws ParseException {
        next(); // consume the '{'
        int index = text.getIndex();
        Map<Object, Object> object = new LinkedHashMap<Object, Object>();
        try {
            consumeWhiteSpace();
            if (text.current() != '}')
                do {
                    Object key = null;
                    consumeWhiteSpace();
                    if (text.current() == '"') {
                        key = parseString();
                    } else if (Character.isJavaIdentifierStart(text.current())) {
                        if (compliance == STRICT)
                            throw new JSONComplianceException("Compliance mode was STRICT, but encountered bare identifier", text.getIndex());
                        key = parseIdentifier();
                    } else if (Character.isDigit(text.current())) {
                        if (compliance == STRICT)
                            throw new JSONComplianceException("Compliance mode was STRICT, but encountered bare number", text.getIndex());
                        key = parseNumber();
                    } else 
                        throw new ParseException("Unexpected character '" + text.current() + "', was expecting an object identifier", text.getIndex());
                    consumeWhiteSpace();
                    if (text.current() != ':')
                        throw new ParseException("Unexpected character '" + text.current() + "', was expecting ':'", text.getIndex());
                    next();
                    Object value = parseValue();
                    object.put(key, value);
                    consumeWhiteSpace();
                    if (text.current() == ',') {
                        next();
                    } else {
                        break;
                    }
                } while (true);
            if (text.current() != '}')
                throw new ParseException("Unexpected character '" + text.current() + "', was expecting '}'", text.getIndex());
            next();
        } catch (JSONComplianceException jce) {
            throw jce;
        } catch (ParseException pe) {
            ParseException pe2 = new ParseException("Error parsing an object", index);
            pe2.initCause(pe);
            throw pe2;
        }
        
        return object;
    }
    
    private List<Object> parseArray() throws ParseException {
        next(); // consume the '['
        List<Object> array = new ArrayList<Object>();
        consumeWhiteSpace();
        if (text.current() != ']')
            do {
                Object value = parseValue();
                array.add(value);
                consumeWhiteSpace();
                if (text.current() == ',') {
                    next();
                } else {
                    break;
                }
            } while (true);
        if (text.current() != ']')
            throw new ParseException("Unexpected character '" + text.current() + "', was expecting ']'", text.getIndex());
        next();
        return array;
    }
    
    private Number parseNumber() throws ParseException {
        int index = text.getIndex();
        boolean fp = false;
        int digits = 0;
        try {
            buff.setLength(0);
            if (text.current() == '-') {
                buff.append(text.current());
                next();
            }
            if (text.current() == '0') {
                buff.append(text.current());
                next();
                digits ++;
                if (digits() > 0)
                    throw new ParseException("Invalid number format - digits following a leading zero are not allowed", text.getIndex());
            } else if (Character.isDigit(text.current())) {
                digits += digits();
            }
            if (text.current() == '.') {
                fp = true;
                buff.append(text.current());
                next();
                int l = digits();
                if (l == 0)
                    throw new ParseException("Expected at least 1 digit, got '" + text.current() + "'", text.getIndex());
                digits += l;
            }
            if (text.current() == 'e' || text.current() == 'E') {
                fp = true;
                buff.append(text.current());
                next();
                if (text.current() == '+' || text.current() == '-') {
                    buff.append(text.current());
                    next();
                }
                if (digits() == 0)
                    throw new ParseException("Expected at least 1 digit, got '" + text.current() + "'", text.getIndex());
            }
        } catch (ParseException pe) {
            ParseException pe2 = new ParseException("Error parsing a number", index);
            pe2.initCause(pe);
            throw pe2;
        }
        try {
            if (fp) {
                if (digits < 17)
                    return Double.valueOf(buff.toString());
                return new BigDecimal(buff.toString());
            } else {
                if (digits < 19)
                    return Long.valueOf(buff.toString());
                return new BigInteger(buff.toString());
            }
        } catch (NumberFormatException nfe) {
            ParseException pe = new ParseException("Error parsing a number", index);
            pe.initCause(nfe);
            throw pe;
        }
    }
    
    private int digits() throws ParseException {
        int c = 0;
        while (Character.isDigit(text.current())) {
            buff.append(text.current());
            next();
            c++;
        }
        return c;
    }
    
    private String parseString() throws ParseException {
        int index = text.getIndex();
        try {
            next(); // skip the opening quote
            buff.setLength(0);
            while (text.current() != '"') {
                if (text.current() == '\\') {
                    next();
                    if (text.current() == 'u') {
                        buff.append(unicode());
                    } else {
                        Character value = escapes.get(new Character(text.current()));
                        if (value != null) {
                            buff.append(value.charValue());
                        } else {
                            throw new ParseException("Unknown escape '\\" + text.current() + "'", text.getIndex());
                        }
                        next();
                    }
                } else {
                    buff.append(text.current());
                    next();
                }
            }
            next();
            return buff.toString();
        } catch (ParseException pe) {
            ParseException pe2 = new ParseException("Error parsing a string", index);
            pe2.initCause(pe);
            throw pe2;
        }
    }

    private void next() throws ParseException {
        if (text.next() == CharacterIterator.DONE) {
            if (end)
                throw new ParseException("End of stream reached", text.getIndex());
            end = true;
        }
    }
    
    private String parseIdentifier() throws ParseException {
        int index = text.getIndex();
        try {
            buff.setLength(0);
            do {
                if (text.current() == '\\') {
                    next();
                    if (text.current() == 'u') {
                        buff.append(unicode());
                    } else {
                        throw new ParseException("Only Unicode escapes are legal in identifiers", text.getIndex());
                    }
                } else {
                    buff.append(text.current());
                }
                next();
            } while (Character.isJavaIdentifierPart(text.current()) || text.current() == '\'');
        } catch (ParseException pe) {
            ParseException pe2 = new ParseException("Error parsing an identifier", index);
            pe2.initCause(pe);
            throw pe2;
        }
        if (!Character.isJavaIdentifierStart(buff.charAt(0)))
            throw new ParseException("Illegal character starting the identifier '" + buff.charAt(0) + "'", index);
        for (int i=1; i<buff.length(); i++)
            if (!Character.isJavaIdentifierPart(buff.charAt(i)))
                throw new ParseException("Illegal character in the identifier '" + buff.charAt(i) + "'", index + i);
        return buff.toString();
    }
    
    private Object parseReservedWord() throws ParseException {
        int index = text.getIndex();
        String literal;
        try {
            literal = parseIdentifier();
        } catch (ParseException pe) {
            ParseException pe2 = new ParseException("Error parsing a reserved word", index);
            pe2.initCause(pe);
            throw pe2;
        }
        if ("true".equals(literal))
            return Boolean.TRUE;
        if ("false".equals(literal))
            return Boolean.FALSE;
        if ("null".equals(literal))
            return null;
        throw new ParseException("Expected 'true', 'false', or 'null', got '" + literal + "'", index);
    }
    
    private char unicode() throws ParseException {
        int index = text.getIndex();
        int value = 0;
        try {
            next();
            for (int i = 0; i < 4; ++i) {
                switch (text.current()) {
                case '0': case '1': case '2': case '3': case '4': 
                case '5': case '6': case '7': case '8': case '9':
                    value = (value << 4) + text.current() - '0';
                    break;
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                    value = (value << 4) + (text.current() - 'a') + 10;
                    break;
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                    value = (value << 4) + (text.current() - 'A') + 10;
                    break;
                default:
                    throw new ParseException("Illegal character encountered '" + text.current() + "'", text.getIndex());
                }
                next();
            }
        } catch (ParseException pe) {
            ParseException pe2 = new ParseException("Error parsing a unicode escape", index);
            pe2.initCause(pe);
            throw pe2;
        }
        if (value < Character.MIN_SUPPLEMENTARY_CODE_POINT)
            return (char) value;
        throw new ParseException("Unsupported multi-character Unicode escape", index);
    }

    private void consumeWhiteSpace() {
        try {
            while (Character.isWhitespace(text.current()))
                next();
        } catch (ParseException pe) {
            // we don't really care if we hit the end here, it will be caught at another point anyway
        }
    }
    
}
