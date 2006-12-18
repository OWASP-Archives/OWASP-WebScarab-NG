/**
 *
 */
package org.owasp.webscarab.util.httpclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author rdawes
 *
 */
public class ConfigurableSSLProtocolSocketFactory implements
        SecureProtocolSocketFactory {

    private Map<String, SSLContext> sslContexts = null;

    private X509TrustManager trustManager;

    private static Log LOG = LogFactory
            .getLog(ConfigurableSSLProtocolSocketFactory.class);

    public ConfigurableSSLProtocolSocketFactory() {
        sslContexts = new HashMap<String, SSLContext>();
    }

    private SSLContext createSSLContext(String host) {
        try {
            TrustManager[] trustmanagers = new TrustManager[] { getTrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustmanagers, null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
            throw new SSLProtocolInitializationError(
                    "Unsupported algorithm exception: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            LOG.error(e.getMessage(), e);
            throw new SSLProtocolInitializationError("Key management exception: "
                    + e.getMessage());
        }
    }

    private SSLContext getSSLContext(String host) {
        SSLContext sslContext = sslContexts.get(host);
        if (sslContext == null) {
            sslContext = createSSLContext(host);
            sslContexts.put(host, sslContext);
        }
        return sslContext;
    }

    /**
     * Attempts to get a new socket connection to the given host within the
     * given time limit.
     * <p>
     * To circumvent the limitations of older JREs that do not support connect
     * timeout a controller thread is executed. The controller thread attempts
     * to create a new socket within the given limit of time. If socket
     * constructor does not return until the timeout expires, the controller
     * terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *
     * @param host
     *            the host name/IP
     * @param port
     *            the port on the host
     * @param clientHost
     *            the local host name/IP to bind the socket to
     * @param clientPort
     *            the port on the local machine
     * @param params
     *            {@link HttpConnectionParams Http connection parameters}
     *
     * @return Socket a new socket
     *
     * @throws IOException
     *             if an I/O error occurs while creating the socket
     * @throws UnknownHostException
     *             if the IP address of the host cannot be determined
     */
    public Socket createSocket(final String host, final int port,
            final InetAddress localAddress, final int localPort,
            final HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketFactory = getSSLContext(host).getSocketFactory();
        if (timeout == 0) {
            return socketFactory.createSocket(host, port, localAddress,
                    localPort);
        } else {
            Socket socket = socketFactory.createSocket();
            SocketAddress localAddr = new InetSocketAddress(localAddress,
                    localPort);
            SocketAddress remoteAddr = new InetSocketAddress(host, port);
            socket.bind(localAddr);
            socket.connect(remoteAddr, timeout);
            return socket;
        }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(String host, int port, InetAddress clientHost,
            int clientPort) throws IOException, UnknownHostException {
        return getSSLContext(host).getSocketFactory().createSocket(host, port,
                clientHost, clientPort);
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return getSSLContext(host).getSocketFactory().createSocket(host, port);
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        return getSSLContext(host).getSocketFactory().createSocket(socket,
                host, port, autoClose);
    }


    /**
     * @return the trustManager
     */
    public X509TrustManager getTrustManager() {
        return this.trustManager;
    }


    /**
     * @param trustManager the trustManager to set
     */
    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

}
