/**
 * 
 */
package org.owasp.webscarab.plugins.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.owasp.webscarab.Annotation;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;
import org.owasp.webscarab.NamedValue;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.util.HttpMethodUtils;

/**
 * @author rdawes
 * 
 */
public class ConnectionHandler implements Runnable {

	private ConversationService conversationService;

	private Annotator annotator;

	private final static String NO_CERTIFICATE = "HTTP/1.0 503 Service unavailable - SSL server certificate not available\r\n\r\n";

	private final static String NO_CERTIFICATE_MESSAGE = "There is no SSL server certificate available for use";

	private Socket socket;

	private String base;

	private HttpClient client = null;

	private SSLSocketFactory sslSocketFactory = null;

	private Logger logger = Logger.getLogger(getClass().getName());

	public ConnectionHandler(Socket socket, String base) {
		this.socket = socket;
		this.base = base;
	}

	public void run() {
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			boolean close;
			do {
				close = false;
				Conversation conversation = readRequest(is);

				// empty request line, connection closed?
				if (conversation == null)
					return;

				if (conversation.getRequestMethod().equals("CONNECT")) {
					if (getSslSocketFactory() == null) {
						os.write(NO_CERTIFICATE.getBytes());
						os.write(NO_CERTIFICATE_MESSAGE.getBytes());
						os.flush();
						return;
					} else {
						os.write("HTTP/1.0 200 Ok\r\n\r\n".getBytes());
						os.flush();
						// start from the beginning to handle this connection
						// over an SSL connection
						this.socket = negotiateSSL();
						this.base = conversation.getRequestUri().toString();
						this.run();
						return;
					}
				}

				// see if we can get an annotation for this conversation
				Annotation annotation = null;
				if (getAnnotator() != null)
					annotation = getAnnotator().getAnnotation();

				HttpMethodUtils.setRequestContent(conversation, is);
				if (client == null)
					client = new HttpClient();
				HttpMethod httpMethod = HttpMethodUtils
						.constructMethod(conversation);
				client.executeMethod(httpMethod);
				HttpMethodUtils.fillResponse(conversation, httpMethod);

				try {
					writeConversationToBrowser(conversation, os);
				} catch (SocketException se) {
					conversation.getResponseContent();
					close = true;
				}
				if (httpMethod != null)
					httpMethod.releaseConnection();
				if (getConversationService() != null) {
					ConversationSummary summary = new ConversationSummary(
							conversation);
					summary.setPlugin("Proxy");
					getConversationService().addConversation(conversation,
							summary);
					if (annotation != null && !"".equals(annotation.getAnnotation())) {
						annotation.setId(summary.getId());
						getConversationService().updateAnnotation(annotation);
					}
				}
				String connection = conversation
						.getResponseHeader("Connection");
				if ("Keep-Alive".equalsIgnoreCase(connection)) {
					close = false;
				} else {
					close = true;
				}
			} while (!close);
		} catch (IllegalArgumentException iae) {
			logger.severe(iae.getMessage());
			iae.printStackTrace();
		} catch (IOException ioe) {
			logger.severe(ioe.getMessage());
			ioe.printStackTrace();
		} finally {
			try {
				if (!socket.isClosed())
					socket.close();
			} catch (IOException ioe) {
			}
		}
	}

	private String readLine(InputStream is) throws IOException {
		StringBuffer line = new StringBuffer();
		int i;
		char c;
		i = is.read();
		if (i == -1)
			throw new IOException("Unexpected end of line");
		while (i > -1 && i != 10 && i != 13) {
			// Convert the int to a char
			c = (char) (i & 0xFF);
			line = line.append(c);
			i = is.read();
		}
		if (i == 13) { // 10 is unix LF, but DOS does 13+10, so read the 10 if
			// we got 13
			is.read();
		}
		// System.out.println(line);
		return line.toString();
	}

	private Conversation readRequest(InputStream is) throws IOException {
		Conversation conversation = new Conversation();
		String requestLine;
		try {
			do {
				requestLine = readLine(is);
			} while (requestLine.trim().equals(""));
		} catch (IOException ioe) {
			System.err.println("Error reading requestLine");
			return null;
		}
		int first = requestLine.indexOf(" ");
		int last = requestLine.lastIndexOf(" ");
		conversation.setRequestMethod(requestLine.substring(0, first));
		if (conversation.getRequestMethod().equals("CONNECT"))
			base = "https://";
		conversation.setRequestVersion(requestLine.substring(last + 1));
		try {
			URI uri;
			if (base == null) {
				uri = new URI(requestLine.substring(first + 1, last));
			} else {
				uri = new URI(base + requestLine.substring(first + 1, last));
			}
			conversation.setRequestUri(uri);
		} catch (URISyntaxException use) {
			IOException ioe = new IOException("URI Syntax exception parsing '"
					+ base + "'");
			ioe.initCause(use);
			throw ioe;
		}

		String previous = null;
		String header = null;
		do {
			header = readLine(is);
			if (header == null)
				throw new IOException(
						"Unexpected null response reading header line");
			if (header.startsWith(" ")) {
				if (previous == null)
					throw new IOException("Malformed header line: '" + header
							+ "'");
				previous = previous.trim() + " " + header.trim();
			} else {
				if (previous != null) {
					int colon = previous.indexOf(":");
					String name = previous.substring(0, colon);
					String value = previous.substring(colon + 1).trim();
					conversation.addRequestHeader(new NamedValue(name, value));
				}
				previous = header;
			}
		} while (!header.equals(""));
		return conversation;
	}

	private void writeConversationToBrowser(Conversation conversation,
			OutputStream os) throws IOException {
		os.write((conversation.getResponseVersion() + " "
				+ conversation.getResponseStatus() + " "
				+ conversation.getResponseMessage() + "\r\n").getBytes());
		NamedValue[] responseHeaders = conversation.getResponseHeaders();
		for (int i = 0; responseHeaders != null && i < responseHeaders.length; i++) {
			os.write((responseHeaders[i].getName() + ": "
					+ responseHeaders[i].getValue() + "\r\n").getBytes());
		}
		os.write("\r\n".getBytes());
		String chunked = conversation.getResponseHeader("Transfer-Encoding");
		ChunkedOutputStream cos = null;
		InputStream cs = conversation.getResponseContentStream();
		if (cs != null) {
			if (chunked != null && chunked.equalsIgnoreCase("chunked")) {
				cos = new ChunkedOutputStream(os);
				os = cos;
			}
			byte[] buff = new byte[4096];
			int got;
			while ((got = cs.read(buff)) > -1)
				os.write(buff, 0, got);
		}
		os.flush();
		if (cos != null)
			cos.finish();
	}

	private Socket negotiateSSL() {
		try {
			SSLSocket sslsock = (SSLSocket) getSslSocketFactory().createSocket(
					socket, socket.getInetAddress().getHostName(),
					socket.getPort(), true);
			sslsock.setUseClientMode(false);
			System.out.println("Finished negotiating SSL - algorithm is "
					+ sslsock.getSession().getCipherSuite());
			return sslsock;
		} catch (Exception e) {
			System.err.println("Error layering SSL over the socket: " + e);
			return null;
		}
	}

	/**
	 * @return Returns the sslSocketFactory.
	 */
	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	/**
	 * @param sslSocketFactory
	 *            The sslSocketFactory to set.
	 */
	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public Annotator getAnnotator() {
		return this.annotator;
	}

	public void setAnnotator(Annotator annotator) {
		this.annotator = annotator;
	}

	public ConversationService getConversationService() {
		return this.conversationService;
	}

	public void setConversationService(ConversationService conversationService) {
		this.conversationService = conversationService;
	}
}
