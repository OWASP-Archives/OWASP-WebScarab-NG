/**
 *
 */
package org.owasp.webscarab.services;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.StreamingConversation;
import org.owasp.webscarab.util.HttpMethodUtils;
import org.owasp.webscarab.util.httpclient.ProxyChooser;

/**
 * @author rdawes
 *
 */
public class HttpService {

    private static Log LOG = LogFactory.getLog(HttpService.class);

    private ProxyChooser proxyChooser;

	private HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();

	/*
	 * We will use this method to allow the user interface to configure how the http service
	 * behaves, e.g. timeouts, etc
	 */
	public HttpConnectionManagerParams getHttpConnectionManagerParams() {
		return httpConnectionManager.getParams();
	}

	private HttpClient getClient() {
		return new HttpClient(httpConnectionManager);
	}

    public void fetchResponse(Conversation conversation) throws IOException {
        HttpMethod method = fetchResponseViaProxy(conversation);
        HttpMethodUtils.fillResponse(conversation, method);
    }

    public void fetchResponse(StreamingConversation conversation) throws IOException {
        HttpMethod method = fetchResponseViaProxy(conversation);
        // we have two identical methods, since HttpMethodUtils.fillResponse() behaves differently
        // for a StreamingConversation
		HttpMethodUtils.fillResponse(conversation, method);
    }

    private HttpMethod fetchResponseViaProxy(Conversation conversation) throws IOException {
        HttpMethod method = HttpMethodUtils.constructMethod(conversation);
        HttpClient client = getClient();
        ProxyChooser pc = getProxyChooser();
        List<Proxy> proxies = null;
        URI uri = conversation.getRequestUri();
        if (pc != null) {
            proxies = pc.select(uri);
        }
        if (proxies == null) {
            proxies = new LinkedList<Proxy>();
            proxies.add(Proxy.NO_PROXY);
        }
        Iterator<Proxy> it = proxies.iterator();
        ConnectException ce = null;
        do {
            Proxy proxy = it.next();
            if (Proxy.NO_PROXY.equals(proxy)) {
                client.getHostConfiguration().setProxyHost(null);
            } else if (Proxy.Type.HTTP.equals(proxy.type())) {
                SocketAddress sa = proxy.address();
                String host;
                int port = 0;
                if (sa instanceof InetSocketAddress) {
                    InetSocketAddress isa = (InetSocketAddress) sa;
                    host = isa.getHostName();
                    port = isa.getPort();
                    client.getHostConfiguration().setProxy(host, port);
                } else {
                    throw new IOException("Unknown proxy SocketAddress type: " + sa.getClass());
                }
            } else {
                throw new IOException("Unsupported proxy type: " + proxy);
            }
            try {
                client.executeMethod(method);
                return method;
            } catch (ConnectException ce2) {
                ce = ce2;
                if (pc != null)
                    pc.connectFailed(uri, proxy.address(), ce2);
            } catch (ProtocolException pe) {
                LOG.error("Request for " + uri + " failed", pe);
            }
        } while (it.hasNext());
        IOException ioe = new IOException("Could not connect to the server");
        ioe.initCause(ce);
        throw ioe;
    }

    public void fetchResponses(ConversationGenerator generator, int threads) {
    	for (int i=0; i<threads; i++) {
    		new FetcherThread(generator).start();
    	}
    }

    private class FetcherThread extends Thread {

    	private ConversationGenerator generator;

    	public FetcherThread(ConversationGenerator generator) {
    		this.generator = generator;
    	}

    	public void run() {
    		Conversation conversation;
    		while ((conversation = generator.getNextRequest()) != null) {
    			try {
    				fetchResponse(conversation);
    				conversation.getResponseContent();
    				generator.responseReceived(conversation);
    			} catch (IOException ioe) {
    				generator.errorFetchingResponse(conversation, ioe);
    			}
    		}
    	}
    }

    /**
     * @return the proxyChooser
     */
    public ProxyChooser getProxyChooser() {
        return this.proxyChooser;
    }

    /**
     * @param proxyChooser the proxyChooser to set
     */
    public void setProxyChooser(ProxyChooser proxyConfigurer) {
        this.proxyChooser = proxyConfigurer;
    }
}
