/**
 *
 */
package org.owasp.webscarab.util.httpclient;

import java.util.prefs.Preferences;

/**
 * @author rdawes
 *
 */
public class PreferencesProxyConfig extends ProxyConfig {

    private static int instances = 0;

    private Preferences prefs;

    public PreferencesProxyConfig() {
        instances++;
        if (instances > 1) {
            instances--;
            throw new IllegalStateException("Cannot have more than one instance of this class");
        }
        prefs = Preferences.userNodeForPackage(ProxyConfig.class);
        super.setHttpProxyHost(prefs.get(PROPERTY_HTTP_PROXYHOST, null));
        int port = prefs.getInt(PROPERTY_HTTP_PROXYPORT, -1);
        super.setHttpProxyPort(port == -1 ? null : new Integer(port));
        super.setHttpsProxyHost(prefs.get(PROPERTY_HTTPS_PROXYHOST, null));
        port = prefs.getInt(PROPERTY_HTTPS_PROXYPORT, -1);
        super.setHttpsProxyPort(port == -1 ? null : new Integer(port));
        super.setNoProxy(prefs.get(ProxyConfig.PROPERTY_NO_PROXY, null));
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.httpclient.ProxyConfig#setHttpProxyHost(java.lang.String)
     */
    @Override
    public void setHttpProxyHost(String httpProxyHost) {
        super.setHttpProxyHost(httpProxyHost);
        prefs.put(ProxyConfig.PROPERTY_HTTP_PROXYHOST, httpProxyHost);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.httpclient.ProxyConfig#setHttpProxyPort(java.lang.Integer)
     */
    @Override
    public void setHttpProxyPort(Integer httpProxyPort) {
        super.setHttpProxyPort(httpProxyPort);
        if (httpProxyPort == null) {
            prefs.remove(ProxyConfig.PROPERTY_HTTP_PROXYPORT);
        } else {
            prefs.putInt(ProxyConfig.PROPERTY_HTTP_PROXYPORT, httpProxyPort.intValue());
        }
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.httpclient.ProxyConfig#setHttpsProxyHost(java.lang.String)
     */
    @Override
    public void setHttpsProxyHost(String httpsProxyHost) {
        super.setHttpsProxyHost(httpsProxyHost);
        prefs.put(ProxyConfig.PROPERTY_HTTPS_PROXYHOST, httpsProxyHost);
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.httpclient.ProxyConfig#setHttpsProxyPort(java.lang.Integer)
     */
    @Override
    public void setHttpsProxyPort(Integer httpsProxyPort) {
        super.setHttpsProxyPort(httpsProxyPort);
        if (httpsProxyPort == null) {
            prefs.remove(ProxyConfig.PROPERTY_HTTPS_PROXYPORT);
        } else {
            prefs.putInt(ProxyConfig.PROPERTY_HTTPS_PROXYPORT, httpsProxyPort.intValue());
        }
    }

    /* (non-Javadoc)
     * @see org.owasp.webscarab.util.httpclient.ProxyConfig#setNoProxy(java.lang.String)
     */
    @Override
    public void setNoProxy(String noProxy) {
        super.setNoProxy(noProxy);
        prefs.put(ProxyConfig.PROPERTY_NO_PROXY, noProxy);
    }

}
