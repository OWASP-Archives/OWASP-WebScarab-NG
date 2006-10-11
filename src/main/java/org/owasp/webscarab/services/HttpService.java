/**
 *
 */
package org.owasp.webscarab.services;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.util.HttpMethodUtils;

/**
 * @author rdawes
 *
 */
public class HttpService {

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
		HttpMethod method = HttpMethodUtils.constructMethod(conversation);
		getClient().executeMethod(method);
		HttpMethodUtils.fillResponse(conversation, method);
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
}
