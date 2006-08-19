/**
 *
 */
package org.owasp.webscarab.services;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.util.HttpMethodUtils;

/**
 * @author rdawes
 *
 */
public class HttpService {

    public void fetchResponse(Conversation conversation) throws IOException {
    	// This is a very naeive implementation of this method
    	// At the very least, we should reuse the same client when called by
    	// a particular thread.
		HttpClient client = new HttpClient();
		HttpMethod method = HttpMethodUtils.constructMethod(conversation);
		client.executeMethod(method);
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
