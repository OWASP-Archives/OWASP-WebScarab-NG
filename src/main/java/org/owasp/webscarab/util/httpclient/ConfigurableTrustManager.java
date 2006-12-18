/**
 *
 */
package org.owasp.webscarab.util.httpclient;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author rdawes
 *
 */
public class ConfigurableTrustManager implements X509TrustManager {

    private X509TrustManager defaultTrustManager;

    private TrustDecider trustDecider;

    private static Log LOG = LogFactory.getLog(ConfigurableTrustManager.class);

    public ConfigurableTrustManager() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);
            TrustManager[] managers = tmf.getTrustManagers();
            for (int i=0; i<managers.length; i++) {
                if (managers[i] instanceof X509TrustManager)
                    defaultTrustManager = (X509TrustManager) managers[i];
            }
            if (defaultTrustManager == null)
                throw new IllegalStateException("DefaultTrustManager may not be null!");
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    public void checkClientTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
        try {
            defaultTrustManager.checkClientTrusted(ax509certificate, s);
        } catch (CertificateException ce) {
            LOG.info("Allowing untrusted client certificate : " + ax509certificate[0].getSubjectDN());
        }
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
        try {
            defaultTrustManager.checkServerTrusted(ax509certificate, s);
        } catch (CertificateException ce) {
            LOG.info("Allowing untrusted server certificate : " + ax509certificate[0].getSubjectDN());
        }
    }

    /* (non-Javadoc)
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    /**
     * @return the trustDecider
     */
    public TrustDecider getTrustDecider() {
        return this.trustDecider;
    }

    /**
     * @param trustDecider the trustDecider to set
     */
    public void setTrustDecider(TrustDecider trustDecider) {
        this.trustDecider = trustDecider;
    }

}
