/**
 *
 */
package org.owasp.webscarab.util.httpclient;

/**
 * @author rdawes
 *
 */
public class SSLProtocolInitializationError extends Error {
    /**
     * Creates a new SSLProtocolInitializationError.
     */
    public SSLProtocolInitializationError() {
        super();
    }

    /**
     * Creates a new SSLProtocolInitializationError with the specified message.
     *
     * @param message
     *            error message
     */
    public SSLProtocolInitializationError(String message) {
        super(message);
    }
}

