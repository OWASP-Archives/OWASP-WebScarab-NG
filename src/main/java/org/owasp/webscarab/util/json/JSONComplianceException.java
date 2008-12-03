/**
 * 
 */
package org.owasp.webscarab.util.json;

/**
 * @author rdawes
 *
 */
public class JSONComplianceException extends java.text.ParseException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JSONComplianceException(String message, int location) {
        super(message, location);
    }
    
}
