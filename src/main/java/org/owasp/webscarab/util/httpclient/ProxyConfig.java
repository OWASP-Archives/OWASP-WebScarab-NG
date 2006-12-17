/**
 *
 */
package org.owasp.webscarab.util.httpclient;

/**
 * @author rdawes
 *
 */
public class ProxyConfig {

    public final static String PROPERTY_HTTP_PROXYHOST = "httpProxyHost";
    public final static String PROPERTY_HTTP_PROXYPORT = "httpProxyPort";
    public final static String PROPERTY_HTTPS_PROXYHOST = "httpsProxyHost";
    public final static String PROPERTY_HTTPS_PROXYPORT = "httpsProxyPort";
    public final static String PROPERTY_NO_PROXY = "noProxy";

    private String httpProxyHost;

    private Integer httpProxyPort;

    private String httpsProxyHost;

    private Integer httpsProxyPort;

    private String noProxy;

    /**
     * @return the httpProxyHost
     */
    public String getHttpProxyHost() {
        return this.httpProxyHost;
    }

    /**
     * @param httpProxyHost the httpProxyHost to set
     */
    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    /**
     * @return the httpProxyPort
     */
    public Integer getHttpProxyPort() {
        return this.httpProxyPort;
    }

    /**
     * @param httpProxyPort the httpProxyPort to set
     */
    public void setHttpProxyPort(Integer httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    /**
     * @return the httpsProxyHost
     */
    public String getHttpsProxyHost() {
        return this.httpsProxyHost;
    }

    /**
     * @param httpsProxyHost the httpsProxyHost to set
     */
    public void setHttpsProxyHost(String httpsProxyHost) {
        this.httpsProxyHost = httpsProxyHost;
    }

    /**
     * @return the httpsProxyPort
     */
    public Integer getHttpsProxyPort() {
        return this.httpsProxyPort;
    }

    /**
     * @param httpsProxyPort the httpsProxyPort to set
     */
    public void setHttpsProxyPort(Integer httpsProxyPort) {
        this.httpsProxyPort = httpsProxyPort;
    }

    /**
     * @return the noProxy
     */
    public String getNoProxy() {
        return this.noProxy;
    }

    /**
     * @param noProxy the noProxy to set
     */
    public void setNoProxy(String noProxy) {
        this.noProxy = noProxy;
    }

}
