/**
 * 
 */
package org.owasp.webscarab.util.json;

import java.io.IOException;
import java.io.Writer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * @author rdawes
 * 
 */
public class JSONWriter {

    public static final int STRICT = 1;

    public static final int JS_EVAL = 2;

    private Stack<Object> stack = new Stack<Object>();

    private Writer writer;

    private int compliance;

    public JSONWriter(Writer writer) {
        this(writer, STRICT);
    }

    public JSONWriter(Writer writer, int compliance) {
        this.writer = writer;
        this.compliance = compliance;
    }

    private boolean isCyclic(Object object) {
        Iterator<Object> it = stack.iterator();
        while (it.hasNext())
            if (it.next() == object)
                return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public void write(Object object) throws IOException {
        if (object == null) {
            writer.write("null");
        } else if (object instanceof Boolean) {
            writer.write(object.toString());
        } else if (object instanceof Number) {
            writer.write(object.toString());
        } else if (object instanceof String) {
            writeString((String) object);
        } else if (object instanceof Character) {
            writeString(object.toString());
        } else if (object instanceof Collection) {
            writeCollection((Collection) object);
        } else if (object instanceof Map) {
            writeMap((Map) object);
        }
        writer.flush();
    }

    private void writeMap(Map<Object, Object> object) throws IOException {
        if (isCyclic(object)) {
            writer.write("null");
            return;
        }
        stack.push(object);
        writer.write("{");
        Iterator<Map.Entry<Object, Object>> it = object.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            if (compliance == STRICT) {
                if (entry.getKey() instanceof String) {
                    writeString((String) entry.getKey());
                } else
                    throw new RuntimeException("Cannot write key '"
                            + entry.getKey() + "' in STRICT mode");
            } else {
                writeKey(entry.getKey());
            }
            writer.write(":");
            write(entry.getValue());
            if (it.hasNext())
                writer.write(",");
        }
        writer.write("}");
        stack.pop();
    }

    private void writeCollection(Collection<Object> object) throws IOException {
        if (isCyclic(object)) {
            writer.write("null");
            return;
        }
        stack.push(object);
        writer.write("[");
        Iterator<Object> it = object.iterator();
        while (it.hasNext()) {
            Object entry = it.next();
            write(entry);
            if (it.hasNext())
                writer.write(",");
        }
        writer.write("]");
        stack.pop();
    }

    private void writeString(String string) throws IOException {
        writer.write("\"");
        writeUnquotedString(string);
        writer.write("\"");
    }
    
    private void writeUnquotedString(String string) throws IOException {
        CharacterIterator it = new StringCharacterIterator(string.toString());
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == '"')
                writer.write("\\\"");
            else if (c == '\\')
                writer.write("\\\\");
            else if (c == '/')
                writer.write("\\/");
            else if (c == '\b')
                writer.write("\\b");
            else if (c == '\f')
                writer.write("\\f");
            else if (c == '\n')
                writer.write("\\n");
            else if (c == '\r')
                writer.write("\\r");
            else if (c == '\t')
                writer.write("\\t");
            else if (Character.isISOControl(c)) {
                unicode(c);
            } else {
                writer.write(c);
            }
        }
    }

    private void writeKey(Object key) throws IOException {
        if (key instanceof String) {
            String string = (String) key;
            if (!Character.isJavaIdentifierStart(string.charAt(0))) 
                throw new RuntimeException("Illegal character in identifier '" + string + "'");
            for (int i=1; i<string.length(); i++)
                if (!Character.isJavaIdentifierPart(string.charAt(i))) 
                    throw new RuntimeException("Illegal character in identifier '" + string + "'");
            writeUnquotedString(string);
        } else if (key instanceof Number) {
            writer.write(key.toString());
        } else 
            throw new RuntimeException("Illegal key: " + key.getClass());
    }

    static char[] hex = "0123456789ABCDEF".toCharArray();

    private void unicode(char c) throws IOException {
        writer.write("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;
            writer.write(hex[digit]);
            n <<= 4;
        }
    }

}
