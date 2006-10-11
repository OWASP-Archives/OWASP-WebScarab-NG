/**
 *
 */
package org.owasp.webscarab.plugins.proxy;

import java.net.URI;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;

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
		return super.getResponseContent();
	}

	public NamedValue[] getResponseFooters() {
		if (!responseFootersChanged)
			return this.delegate.getResponseFooters();
		return super.getResponseFooters();
	}

	public NamedValue[] getResponseHeaders() {
		if (!responseHeadersChanged)
			return this.delegate.getResponseHeaders();
		return super.getResponseHeaders();
	}

	public String getResponseMessage() {
		if (!responseMessageChanged)
			return this.delegate.getResponseMessage();
		return super.getResponseMessage();
	}

	public String getResponseStatus() {
		if (!responseStatusChanged)
			return this.delegate.getResponseStatus();
		return super.getResponseStatus();
	}

	public String getResponseVersion() {
		if (!responseVersionChanged)
			return this.delegate.getResponseVersion();
		return super.getResponseVersion();
	}

	public void addRequestHeader(NamedValue header) {
		throw new UnsupportedOperationException("Modifying the request is not supported");
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
		if (!responseContentChanged)
			this.delegate.getResponseContent(); // make sure that we have read all the incoming response
		super.setResponseContent(content);
		responseContentChanged = true;
	}

	public void setResponseFooters(NamedValue[] responseFooters) {
		super.setResponseFooters(responseFooters);
		responseFootersChanged = true;
	}

	public void setResponseHeaders(NamedValue[] headers) {
		super.setResponseHeaders(headers);
		responseHeadersChanged = true;
	}

	public void setResponseMessage(String message) {
		super.setResponseMessage(message);
		responseMessageChanged = true;
	}

	public void setResponseStatus(String status) {
		super.setResponseStatus(status);
		responseStatusChanged = true;
	}

	public void setResponseVersion(String version) {
		super.setResponseVersion(version);
		responseVersionChanged = true;
	}
}
