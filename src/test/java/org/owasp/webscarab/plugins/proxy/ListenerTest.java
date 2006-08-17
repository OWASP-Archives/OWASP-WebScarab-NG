/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.util.logging.Logger;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;

import junit.framework.TestCase;

/**
 * @author rdawes
 * 
 */
public class ListenerTest extends TestCase {

	private Proxy.Listener listener;

	private Thread listenerThread;

	private Logger logger = Logger.getLogger(getClass().getName());
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ListenerTest.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ListenerConfiguration config = new ListenerConfiguration();
		config.setHostName("localhost");
		config.setPort(8008);
		listener = new Proxy().new Listener(config);
		listenerThread = new Thread(listener);
		listenerThread.setDaemon(true);
		listenerThread.start();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		listener.stop();
		try {
			while (listener.isRunning()) {
				Thread.sleep(100);
				logger.fine("Waiting for the listener to stop");
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		if (listenerThread.isAlive())
			logger.severe("ListenerThread is still alive!");
	}

	public void testGet() throws Exception {
		HttpClient client = new HttpClient();
		
		HostConfiguration hc = new HostConfiguration();
		hc.setHost("localhost", 80, Protocol.getProtocol("http"));
		hc.setProxy("localhost", 8008);
		client.setHostConfiguration(hc);
        // Provide custom retry handler if necessary
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(0, false));
		
		GetMethod get = new GetMethod("http://localhost:80/WebScarab-test/TestGet.jsp");
        get.setFollowRedirects(false);
        
        
		client.executeMethod(get);
		String body = get.getResponseBodyAsString();
		logger.info("Got first response: " + body.length());
		assertEquals("Response content size", 231, body.length());
		get.releaseConnection();
		get = new GetMethod("http://localhost:80/WebScarab-test/TestGet.jsp");
        get.setFollowRedirects(false);
        // Provide custom retry handler if necessary
        get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(0, false));
		logger.info("Executing part 2");
		client.executeMethod(get);
		logger.info("Now getting the body");
		body = get.getResponseBodyAsString();
		logger.info("Got second response: " + body.length());
		assertEquals("Response content size", 231, body.length());
		get.releaseConnection();
	}
}
