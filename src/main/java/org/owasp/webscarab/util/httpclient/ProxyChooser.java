/**
 *
 */
package org.owasp.webscarab.util.httpclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.owasp.webscarab.util.Glob;

/**
 * @author rdawes
 *
 */
public class ProxyChooser extends ProxySelector {

    private ProxyConfig proxyConfig = null;

    public List<Proxy> select(URI uri) {
        Proxy proxy = Proxy.NO_PROXY;
        if (proxyConfig == null || isNoProxy(uri)) {
            // do nothing
        } else if ("http".equalsIgnoreCase(uri.getScheme())) {
            String host = proxyConfig.getHttpProxyHost();
            Integer port = proxyConfig.getHttpProxyPort();
            if ( host != null && port != null) {
                InetSocketAddress address = new InetSocketAddress(host, port.intValue());
                proxy = new Proxy(Proxy.Type.HTTP, address);
            }
        } else if ("https".equalsIgnoreCase(uri.getScheme())) {
            String host = proxyConfig.getHttpsProxyHost();
            Integer port = proxyConfig.getHttpsProxyPort();
            if ( host != null && port != null) {
                InetSocketAddress address = new InetSocketAddress(host, port.intValue());
                proxy = new Proxy(Proxy.Type.HTTP, address);
            }
        }
        List<Proxy> proxies = new LinkedList<Proxy>();
        proxies.add(proxy);
        return proxies;
    }

    private boolean isNoProxy(URI uri) {
        if (proxyConfig.getNoProxy() == null) return false;
        String[] noProxy = proxyConfig.getNoProxy().split("[ ,;] *");
        String host = uri.getHost().toLowerCase();
        for (int i=0; i<noProxy.length; i++) {
            noProxy[i] = noProxy[i].toLowerCase();
            if (host.endsWith(noProxy[i]) || host.matches(Glob.globToRE(noProxy[i])))
                return true;
        }
        return false;
    }

    public void connectFailed(URI uri, SocketAddress address, IOException ioe) {
        // we do nothing at this stage
    }

    /**
     * @return the proxyConfig
     */
    public ProxyConfig getProxyConfig() {
        return this.proxyConfig;
    }

    /**
     * @param proxyConfig the proxyConfig to set
     */
    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

}
