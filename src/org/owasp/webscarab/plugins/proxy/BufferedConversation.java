/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;

/**
 * @author rdawes
 *
 */
public class BufferedConversation extends Conversation {

	private Conversation delegate;
	
	private boolean responseVersionChanged = false;
	
	private boolean responseStatusChanged = false;
	
	private boolean responseMessageChanged = false;
	
	private boolean responseHeadersChanged = false;
	
	private boolean responseContentChanged = false;
	
	private boolean responseFootersChanged = false;
	
	public BufferedConversation(Conversation conversation) {
		this.delegate = conversation;
	}

	public void addRequestHeader(NamedValue header) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public byte[] getProcessedRequestContent() {
		return this.delegate.getProcessedRequestContent();
	}

	public byte[] getRequestContent() {
		return this.delegate.getRequestContent();
	}

	public String getRequestHeader(String name) {
		return this.delegate.getRequestHeader(name);
	}

	public NamedValue[] getRequestHeaders() {
		return this.delegate.getRequestHeaders();
	}

	public NamedValue[] getRequestHeaders(String name) {
		return this.delegate.getRequestHeaders(name);
	}

	public String getRequestMethod() {
		return this.delegate.getRequestMethod();
	}

	public URI getRequestUri() {
		return this.delegate.getRequestUri();
	}

	public String getRequestVersion() {
		return this.delegate.getRequestVersion();
	}

	public byte[] getResponseContent() {
		if (!responseContentChanged)
			return this.delegate.getResponseContent();
		
		if (responseContentStream != null) {
			flushContentStream(responseContentStream);
			this.responseContentStream = null;
		}
		if (this.responseContent == null)
			return null;
		return this.responseContent.toByteArray();
	}

	public InputStream getResponseContentStream() {
		if (!responseContentChanged)
			return this.delegate.getResponseContentStream();
		return this.responseContentStream;
	}

	public NamedValue[] getResponseFooters() {
		if (!responseFootersChanged)
			return this.delegate.getResponseFooters();
		return this.responseFooters;
	}

	public NamedValue[] getResponseHeaders() {
		if (!responseHeadersChanged)
			return this.delegate.getResponseHeaders();
		return this.responseHeaders;
	}

	public String getResponseMessage() {
		if (!responseMessageChanged)
			return this.delegate.getResponseMessage();
		return this.responseMessage;
	}

	public String getResponseStatus() {
		if (!responseStatusChanged)
			return this.delegate.getResponseStatus();
		return this.responseStatus;
	}

	public String getResponseVersion() {
		if (!responseVersionChanged)
			return this.delegate.getResponseVersion();
		return this.responseVersion;
	}

	public NamedValue[] removeRequestHeaders(String name) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setProcessedRequestContent(byte[] content) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setRequestContent(byte[] content) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setRequestHeader(NamedValue header) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setRequestHeaders(NamedValue[] headers) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setRequestMethod(String method) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setRequestUri(URI uri) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setRequestVersion(String version) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
	}

	public void setResponseContent(byte[] content) {
		this.delegate.getResponseContent(); // make sure that we have read all the incoming response
		if (content == null) {
			this.responseContent = null;
		} else {
			this.responseContent = new ByteArrayOutputStream();
			try {
				this.responseContent.write(content);
			} catch (IOException ioe) {
			}
		}
		responseContentChanged = true;
	}

	public void setResponseContentStream(InputStream contentStream) {
		this.delegate.getResponseContent(); // make sure that we have read all the incoming response
		if (contentStream == null) {
			this.responseContentStream = null;
			this.responseContent = null;
		} else {
			this.responseContent = new ByteArrayOutputStream();
			this.responseContentStream = new CopyInputStream(contentStream,
					responseContent);
		}
		responseContentChanged = true;
	}

	public void setResponseFooters(NamedValue[] responseFooters) {
		this.responseFooters = responseFooters;
		responseFootersChanged = true;
	}

	public void setResponseHeaders(NamedValue[] headers) {
		this.responseHeaders = headers;
		responseHeadersChanged = true;
	}

	public void setResponseMessage(String message) {
		this.responseMessage = message;
		responseMessageChanged = true;
	}

	public void setResponseStatus(String status) {
		this.responseStatus = status;
		responseStatusChanged = true;
	}

	public void setResponseVersion(String version) {
		this.responseVersion = version;
		responseVersionChanged = true;
	}
}
