/*
 * DefaultConversation.java
 *
 * Created on 09 March 2006, 06:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab.domain;

import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * The <code>Conversation</code> is the basic element used by WebScarab. It
 * represents a request sent to the server, and the response that was returned
 * from the server
 *
 * It provides a number of self-explanatory getters and setters for the various
 * attributes of a request and response
 *
 * Possibly worthy of some explanation are the "processedContent" methods. These
 * methods handle raw content, possibly gzipped, or otherwise encoded, and
 * provide a handy way of getting to the useful content inside
 *
 * @author rdawes
 */

public class Conversation extends BaseEntity implements Comparable<Conversation> {

	public static final String PROPERTY_CONVERSATION_DATE = "date";

	public static final String PROPERTY_CONVERSATION_SOURCE = "source";

	public static final String PROPERTY_REQUEST_METHOD = "requestMethod";

	public static final String PROPERTY_REQUEST_URI = "requestUri";

	public static final String PROPERTY_REQUEST_VERSION = "requestVersion";

	public static final String PROPERTY_REQUEST_HEADERS = "requestHeaders";

	public static final String PROPERTY_REQUEST_CONTENT = "requestContent";

	public static final String PROPERTY_REQUEST_CONTENT_SIZE = "requestContentSize";

	public static final String PROPERTY_REQUEST_PROCESSED_CONTENT = "processedRequestContent";

	public static final String PROPERTY_RESPONSE_VERSION = "responseVersion";

	public static final String PROPERTY_RESPONSE_STATUS = "responseStatus";

	public static final String PROPERTY_RESPONSE_MESSAGE = "responseMessage";

	public static final String PROPERTY_RESPONSE_HEADERS = "responseHeaders";

	public static final String PROPERTY_RESPONSE_CONTENT = "responseContent";

	public static final String PROPERTY_RESPONSE_CONTENT_SIZE = "responseContentSize";

	public static final String PROPERTY_RESPONSE_PROCESSED_CONTENT = "processedResponseContent";

	public static final String PROPERTY_RESPONSE_FOOTERS = "responseFooters";

	public static final String ENCODING = "Content-Encoding";

	private Date date = new Date();

	private String source;

	private String requestMethod;

	private URI requestUri;

	private String requestVersion;

	private NamedValue[] requestHeaders;

	private byte[] requestContent;

	private String responseVersion;

	private String responseStatus;

	private String responseMessage;

	private NamedValue[] responseHeaders;

	private byte[] responseContent = null;

	private NamedValue[] responseFooters;

	protected Logger logger = Logger.getLogger(getClass().getName());

	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	
	/** Creates a new instance of DefaultConversation */
	public Conversation() {
	}

	/**
	 * This value is automatically initialised to the current time when the
	 * Conversation is constructed
	 *
	 * @return Returns the date.
	 */
	public Date getDate() {
		if (this.date == null)
			return null;
		return new Date(date.getTime());
	}

	/**
	 * @param date
	 *            The date to set.
	 */
	public void setDate(Date date) {
	    Date old = this.date;
	    boolean changed = false;
		if (date == null) {
			this.date = null;
			if (old != null)
			    changed = true;
		} else {
			this.date = new Date(date.getTime());
			if (old == null || date.getTime() != old.getTime())
			    changed = true;
		}
		if (changed)
		    changeSupport.firePropertyChange(PROPERTY_CONVERSATION_DATE, old, date);
	}

    /**
     * @return Returns the source.
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            The source to set.
     */
    public void setSource(String plugin) {
        String old = this.source;
        this.source = plugin;
        if (old != plugin && (old != null && !old.equals(plugin)))
            changeSupport.firePropertyChange(PROPERTY_CONVERSATION_SOURCE, old, plugin);
    }

	public String getRequestMethod() {
		return this.requestMethod;
	}

	public void setRequestMethod(String method) {
	    String old = this.requestMethod;
		this.requestMethod = method;
        if (old != method && (old != null && !old.equals(method)))
                changeSupport.firePropertyChange(PROPERTY_REQUEST_METHOD, old, method);
	}

	public URI getRequestUri() {
		return this.requestUri;
	}

	public void setRequestUri(URI uri) {
	    URI old = this.requestUri;
		this.requestUri = uri;
        if (old != uri && (old != null && !old.equals(uri)))
                changeSupport.firePropertyChange(PROPERTY_REQUEST_URI, old, uri);
	}

	public String getRequestVersion() {
		return this.requestVersion;
	}

	public void setRequestVersion(String version) {
	    String old = this.requestVersion;
		this.requestVersion = version;
        if (old != version && (old != null && !old.equals(version)))
                changeSupport.firePropertyChange(PROPERTY_REQUEST_VERSION, old, version);
	}

	public NamedValue[] getRequestHeaders() {
		return NamedValue.copy(this.requestHeaders);
	}

	public void setRequestHeaders(final NamedValue[] headers) {
	    NamedValue[] old = requestHeaders;
		this.requestHeaders = NamedValue.copy(headers);
		// fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_REQUEST_HEADERS, old, headers);
	}

	public NamedValue[] getRequestHeaders(final String name) {
		return NamedValue.find(name, getRequestHeaders());
	}

	public String getRequestHeader(final String name) {
		final NamedValue[] results = getRequestHeaders(name);
		String value = null;
		if (results != null) {
			if (results.length == 1) {
				value = results[0].getValue();
			} else if (results.length > 1) {
				throw new RuntimeException("Expected only a single result for "
						+ name + ", got " + results.length);
			}
		}
		return value;
	}

	public void setRequestHeader(final NamedValue header) {
		removeRequestHeaders(header.getName());
		addRequestHeader(header);
	}

	public void addRequestHeader(final NamedValue header) {
		setRequestHeaders(NamedValue.add(getRequestHeaders(), header));
	}

	public NamedValue[] removeRequestHeaders(final String name) {
		final NamedValue[] headers = getRequestHeaders();
		final NamedValue[] found = NamedValue.find(name, headers);
		if (found != null && found.length > 0) {
			setRequestHeaders(NamedValue.delete(name, headers));
		}
		return found;
	}

	public byte[] getRequestContent() {
		return copyContent(this.requestContent);
	}

	public int getRequestContentSize() {
		byte[] content = getRequestContent();
		if (content == null) return 0;
		return content.length;
	}

	public void setRequestContent(final byte[] content) {
	    byte[] old = requestContent;
		this.requestContent = copyContent(content);
	      // fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_REQUEST_CONTENT, old, requestContent);
        updateRequestContentLength();
	}

	public byte[] getProcessedRequestContent() {
		return processContent(getRequestHeaders(ENCODING), getRequestContent(),
				true);
	}

	public void setProcessedRequestContent(final byte[] content) {
	    byte[] old = this.requestContent;
		this.requestContent = processContent(getRequestHeaders(ENCODING),
				content, false);
        // fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_REQUEST_CONTENT, old, requestContent);
        updateRequestContentLength();
	}

	private void updateRequestContentLength() {
	    NamedValue[] contentLength = getRequestHeaders("Content-Length");
	    if (contentLength != null && contentLength.length > 0) {
	        String length = Integer.toString(requestContent == null ? 0 : requestContent.length);
	        setRequestHeader(new NamedValue("Content-Length", length));
	    }
	}

	public String getResponseVersion() {
		return this.responseVersion;
	}

	public void setResponseVersion(final String version) {
	    String old = this.responseVersion;
		this.responseVersion = version;
        if (old != version && (old != null && !old.equals(version)))
                changeSupport.firePropertyChange(PROPERTY_RESPONSE_VERSION, old, version);
	}

	public String getResponseMessage() {
		return this.responseMessage;
	}

	public void setResponseMessage(final String message) {
	    String old = this.responseMessage;
		this.responseMessage = message;
        if (old != message && (old != null && !old.equals(message)))
            changeSupport.firePropertyChange(PROPERTY_RESPONSE_MESSAGE, old, message);
	}

	public String getResponseStatus() {
		return this.responseStatus;
	}

	public void setResponseStatus(final String status) {
	    String old = this.responseStatus;
		this.responseStatus = status;
        if (old != status && (old != null && !old.equals(status)))
            changeSupport.firePropertyChange(PROPERTY_RESPONSE_STATUS, old, status);
	}

	public NamedValue[] getResponseHeaders() {
		return NamedValue.copy(this.responseHeaders);
	}

	public void setResponseHeaders(final NamedValue[] headers) {
	    NamedValue[] old = this.responseHeaders;
		this.responseHeaders = NamedValue.copy(headers);
        // fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_RESPONSE_HEADERS, old, headers);
	}

	public NamedValue[] getResponseHeaders(final String name) {
		return NamedValue.find(name, getResponseHeaders());
	}

	public String getResponseHeader(final String name) {
		final NamedValue[] results = getResponseHeaders(name);
		String value = null;
		if (results != null) {
			if (results.length == 1) {
				value = results[0].getValue();
			} else if (results.length > 1) {
				throw new RuntimeException("Expected only a single result for "
						+ name + ", got " + results.length);
			}
		}
		return value;
	}

	public void setResponseHeader(final NamedValue header) {
		removeResponseHeaders(header.getName());
		addResponseHeader(header);
	}

	public void addResponseHeader(final NamedValue header) {
		setResponseHeaders(NamedValue.add(getResponseHeaders(), header));
	}

	public NamedValue[] removeResponseHeaders(final String name) {
		final NamedValue[] headers = getResponseHeaders();
		final NamedValue[] found = NamedValue.find(name, headers);
		if (found != null && found.length > 0) {
			setResponseHeaders(NamedValue.delete(name, headers));
		}
		return found;
	}

	public byte[] getResponseContent() {
		return copyContent(responseContent);
	}

	public int getResponseContentSize() {
		byte[] content = getResponseContent();
		if (content == null) return 0;
		return content.length;
	}

	public void setResponseContent(final byte[] content) {
	    byte[] old = responseContent;
		responseContent = copyContent(content);
        // fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_RESPONSE_CONTENT, old, responseContent);
        updateResponseContentLength();
	}

	public byte[] getProcessedResponseContent() {
		return processContent(getResponseHeaders(ENCODING),
				getResponseContent(), true);
	}

	public void setProcessedResponseContent(final byte[] content) {
	    byte[] old = this.responseContent;
		byte[] processed = processContent(getResponseHeaders(ENCODING),
				content, false);
		setResponseContent(processed);
        // fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_RESPONSE_CONTENT, old, responseContent);
        updateResponseContentLength();
	}

    private void updateResponseContentLength() {
        NamedValue[] contentLength = getResponseHeaders("Content-Length");
        if (contentLength != null) {
            String length = Integer.toString(responseContent == null ? 0 : responseContent.length);
            setResponseHeader(new NamedValue("Content-Length", length));
        }
    }

	/**
	 * @return Returns the responseFooters.
	 */
	public NamedValue[] getResponseFooters() {
		return NamedValue.copy(this.responseFooters);
	}

	/**
	 * @param responseFooters
	 *            The responseFooters to set.
	 */
	public void setResponseFooters(final NamedValue[] responseFooters) {
	    NamedValue[] old = this.responseFooters;
		this.responseFooters = NamedValue.copy(responseFooters);
        // fire unconditionally
        changeSupport.firePropertyChange(PROPERTY_RESPONSE_FOOTERS, old, responseFooters);
	}

	public NamedValue[] getResponseFooters(final String name) {
		return NamedValue.find(name, getResponseFooters());
	}

	public String getResponseFooter(final String name) {
		NamedValue[] results = getResponseFooters(name);
		String value = null;
		if (results != null) {
			if (results.length == 1) {
				value = results[0].getValue();
			} else if (results.length > 1) {
				throw new RuntimeException("Expected only a single result for "
						+ name + ", got " + results.length);
			}
		}
		return value;
	}

	public void addResponseFooter(final NamedValue header) {
		setResponseFooters(NamedValue.add(getResponseFooters(), header));
	}

	public NamedValue[] removeResponseFooter(final String name) {
		NamedValue[] headers = getResponseFooters();
		NamedValue[] found = NamedValue.find(name, headers);
		if (found != null && found.length > 0) {
			setResponseFooters(NamedValue.delete(name, headers));
		}
		return found;
	}

	/* Utility methods follow */

	public static byte[] copyContent(final byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return bytes;
		byte[] copy = new byte[bytes.length];
		System.arraycopy(bytes, 0, copy, 0, bytes.length);
		return copy;
	}

	/**
	 * @param coding
	 * @param content
	 * @param get
	 * @return
	 */
	public static byte[] processContent(final NamedValue[] coding,
			final byte[] content, final boolean get) {
		if (content == null || content.length == 0)
			return content;
		byte[] result = content;
		try {
			if (get) {
				if (coding != null) {
					for (int i = 0; i < coding.length; i++) {
						final String token = coding[i].getValue();
						InputStream is = new ByteArrayInputStream(result);
						if (token.equalsIgnoreCase("gzip")
								|| token.equalsIgnoreCase("x-gzip")) {
							is = new GZIPInputStream(is);
						} else if (token.equalsIgnoreCase("deflate")) {
							is = new InflaterInputStream(is);
						} else if (token.equalsIgnoreCase("identity")) {
							continue;
						} else
							throw new RuntimeException("Unknown coding type: "
									+ token);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] buff = new byte[4096];
						int got;
						while ((got = is.read(buff)) > 0) {
							baos.write(buff, 0, got);
						}
						baos.flush();
						result = baos.toByteArray();
					}
				}
			} else {
				if (coding != null) {
					for (int i = 0; i < coding.length; i++) {
						String token = coding[i].getValue();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						OutputStream os = baos;
						if (token.equalsIgnoreCase("gzip")
								|| token.equalsIgnoreCase("x-gzip")) {
							os = new GZIPOutputStream(os);
						} else if (token.equalsIgnoreCase("deflate")) {
							os = new DeflaterOutputStream(os);
						} else if (token.equalsIgnoreCase("identity")) {
							continue;
						} else
							throw new RuntimeException("Unknown coding type: "
									+ token);
						os.write(result);
						os.flush();
						result = baos.toByteArray();
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Exception processing content encoding",
					ioe);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Conversation that) {
        return this.getDate().compareTo(that.getDate());
	}

    public Conversation clone() {
        Conversation conversation = new Conversation();
        conversation.setRequestMethod(getRequestMethod());
        conversation.setRequestUri(getRequestUri());
        conversation.setRequestVersion(getRequestVersion());
        conversation.setRequestHeaders(getRequestHeaders());
        conversation.setRequestContent(getRequestContent());
        conversation.setResponseVersion(getResponseVersion());
        conversation.setResponseStatus(getResponseStatus());
        conversation.setResponseMessage(getResponseMessage());
        conversation.setResponseHeaders(getResponseHeaders());
        conversation.setResponseContent(getResponseContent());
        conversation.setResponseFooters(getResponseFooters());
        return conversation;
    }

//    public void addPropertyChangeListener(PropertyChangeListener listener) {
//        changeSupport.addPropertyChangeListener(listener);
//    }
//    
//    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
//        changeSupport.addPropertyChangeListener(propertyName, listener);
//    }
//    
//    public void removePropertyChangeListener(PropertyChangeListener listener) {
//        changeSupport.removePropertyChangeListener(listener);
//    }
//    
//    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
//        changeSupport.removePropertyChangeListener(propertyName, listener);
//    }
}
