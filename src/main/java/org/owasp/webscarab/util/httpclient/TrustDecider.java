/**
 *
 */
package org.owasp.webscarab.util.httpclient;

import java.security.cert.X509Certificate;

/**
 * @author rdawes
 *
 */
public interface TrustDecider {

    boolean trust(X509Certificate[] certificates, boolean caNotValid, boolean beforeValid, boolean afterValid);

    public static class PermissiveTrustDecider implements TrustDecider {

        public boolean trust(X509Certificate[] certificates, boolean caNotValid, boolean beforeValid, boolean afterValid) {
            return true;
        }

    }
}
