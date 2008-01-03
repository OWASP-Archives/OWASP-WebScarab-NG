/**
 * 
 */
package org.owasp.webscarab.util.json;

/**
 * @author rdawes
 *
 */
public class JSONComplianceException extends java.text.ParseException {

    public JSONComplianceException(String message, int location) {
        super(message, location);
    }
    
}
