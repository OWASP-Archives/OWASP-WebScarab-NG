/*
 * DefaultConversation.java
 *
 * Created on 09 March 2006, 06:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.owasp.webscarab;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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

public class Conversation extends BaseEntity {

    public static final String PROPERTY_REQUEST_METHOD = "requestMethod";

    public static final String PROPERTY_REQUEST_URL = "requestUrl";

    public static final String PROPERTY_REQUEST_VERSION = "requestVersion";

    public static final String PROPERTY_REQUEST_HEADERS = "requestHeaders";

    public static final String PROPERTY_REQUEST_CONTENT = "requestContent";

    public static final String PROPERTY_RESPONSE_VERSION = "responseVersion";

    public static final String PROPERTY_RESPONSE_STATUS = "responseStatus";

    public static final String PROPERTY_RESPONSE_MESSAGE = "responseMessage";

    public static final String PROPERTY_RESPONSE_HEADERS = "responseHeaders";

    public static final String PROPERTY_RESPONSE_CONTENT = "responseContent";

    public static final String PROPERTY_RESPONSE_FOOTERS = "responseFooters";

	private static final String ENCODING = "Content-Encoding";

	private Date date = new Date();

	private String requestMethod;

	private URL requestUrl;

	private String requestVersion;

	private NamedValue[] requestHeaders;

	private byte[] requestContent;

	private String responseVersion;

	private String responseStatus;

	private String responseMessage;

	private NamedValue[] responseHeaders;

	private CopyInputStream responseContentStream = null;

	private ByteArrayOutputStream responseContent = null;

	private NamedValue[] responseFooters;

	private Logger logger = Logger.getLogger(getClass().getName());

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
		if (date == null) {
			this.date = null;
		} else {
			this.date = new Date(date.getTime());
		}
	}

	public String getRequestMethod() {
		return this.requestMethod;
	}

	public void setRequestMethod(String method) {
		this.requestMethod = method;
	}

	public URL getRequestUrl() {
		return this.requestUrl;
	}

	public void setRequestUrl(URL url) {
		this.requestUrl = url;
	}

	public String getRequestVersion() {
		return this.requestVersion;
	}

	public void setRequestVersion(String version) {
		this.requestVersion = version;
	}

	public NamedValue[] getRequestHeaders() {
		return NamedValue.copy(this.requestHeaders);
	}

	public void setRequestHeaders(final NamedValue[] headers) {
		this.requestHeaders = NamedValue.copy(headers);
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

	public void setRequestContent(final byte[] content) {
		this.requestContent = copyContent(content);
	}

	public byte[] getProcessedRequestContent() {
		return processContent(getRequestHeaders(ENCODING), getRequestContent(),
				true);
	}

	public void setProcessedRequestContent(final byte[] content) {
		this.requestContent = processContent(getRequestHeaders(ENCODING),
				content, false);
	}

	public String getResponseVersion() {
		return this.responseVersion;
	}

	public void setResponseVersion(final String version) {
		this.responseVersion = version;
	}

	public String getResponseMessage() {
		return this.responseMessage;
	}

	public void setResponseMessage(final String message) {
		this.responseMessage = message;
	}

	public String getResponseStatus() {
		return this.responseStatus;
	}

	public void setResponseStatus(final String status) {
		this.responseStatus = status;
	}

	public NamedValue[] getResponseHeaders() {
		return NamedValue.copy(this.responseHeaders);
	}

	public void setResponseHeaders(final NamedValue[] headers) {
		this.responseHeaders = NamedValue.copy(headers);
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

	public InputStream getResponseContentStream() {
		return responseContentStream;
	}

	public void setResponseContentStream(InputStream contentStream) {
		if (contentStream == null) {
			this.responseContentStream = null;
			this.responseContent = null;
		} else {
			this.responseContent = new ByteArrayOutputStream();
			this.responseContentStream = new CopyInputStream(contentStream,
					responseContent);
		}
	}

	public byte[] getResponseContent() {
		if (responseContentStream != null) {
			flushContentStream(responseContentStream);
			this.responseContentStream = null;
		}
		if (this.responseContent == null)
			return null;
		return this.responseContent.toByteArray();
	}

	public void setResponseContent(final byte[] content) {
		if (content == null) {
			this.responseContent = null;
		} else {
			this.responseContent = new ByteArrayOutputStream();
			try {
				this.responseContent.write(content);
			} catch (IOException ioe) {
				logger.severe("Impossible IOException! " + ioe.getMessage());
			}
		}
	}

	public byte[] getProcessedResponseContent() {
		return processContent(getResponseHeaders(ENCODING),
				getResponseContent(), true);
	}

	public void setProcessedResponseContent(final byte[] content) {
		byte[] processed = processContent(getResponseHeaders(ENCODING),
				content, false);
		setResponseContent(processed);
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
		this.responseFooters = NamedValue.copy(responseFooters);
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

	private void flushContentStream(InputStream is) {
		if (is != null) {
			try {
				byte[] buff = new byte[2048];
				while (is.read(buff) > 0)
					; // flush whatever was in the stream
			} catch (IOException ioe) {
			} // ignore any errors
		}
	}

	private class CopyInputStream extends FilterInputStream {

		private OutputStream os;

		public CopyInputStream(InputStream is, OutputStream os) {
			super(is);
			this.os = os;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilterInputStream#read()
		 */
		@Override
		public int read() throws IOException {
			int result = super.read();
			if (result > -1) {
				os.write(result);
				// String text = getResponseHeader("Content-Type");
				// if (text != null && text.startsWith("text"))
				// System.out.write(result);
				os.flush();
			} else {
				// we close to signal downstream readers that the inputstream
				// has closed
				os.close();
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FilterInputStream#read(byte[], int, int)
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int num = super.read(b, off, len);
			if (num > 0) {
				os.write(b, off, num);
				// String text = getResponseHeader("Content-Type");
				// if (text != null && text.startsWith("text"))
				// System.out.write(b, off, num);
				os.flush();
			} else {
				os.close();
			}
			return num;
		}
	}

}
