/**
 * 
 */
package org.owasp.webscarab.ui;

import java.net.URI;

import javax.swing.tree.TreePath;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;
import org.springframework.richclient.tree.AbstractTreeModel;

/**
 * @author rdawes
 * 
 */
public class ConversationTreeModel extends AbstractTreeModel {

	public final static boolean REQUEST = true;

	public final static boolean RESPONSE = false;

	private String requestLine = null;

	private String statusLine = null;

	private NamedValue[] NO_HEADERS = new NamedValue[0];

	private NamedValue[] requestHeaders = null;

	private String[] cookies = null;

	private NamedValue[] responseHeaders = null;

	private String[] queryParameters = null;

	private boolean which = REQUEST;

	public ConversationTreeModel(Conversation conversation, boolean which) {
		super(conversation);
		this.which = which;
	}

	public Object getChild(Object parent, int index) {
		return getChildren(parent)[index];
	}

	public int getChildCount(Object parent) {
		return getChildren(parent).length;
	}

	public int getIndexOfChild(Object parent, Object child) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isLeaf(Object node) {
		return getChildCount(node) == 0;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
//		logger.info("Path " + path + " changed to " + newValue);
		// TODO Auto-generated method stub

	}

	private Object[] getChildren(Object parent) {
		Conversation c = (Conversation) getRoot();
		NamedValue[] cookies;
		if (parent == c) {
			return new Object[] {
					which == REQUEST ? getRequestLine(c) : getStatusLine(c),
					which == REQUEST ? getRequestHeaders(c)
							: getResponseHeaders(c) };
		} else if (parent == getRequestLine(c)) {
			URI uri = c.getRequestUri();
			if (uri == null) return new Object[0];
			return getQueryParameters(uri.getQuery());
		} else if (parent == getRequestHeaders(c)) {
			return getRequestHeaders(c);
		} else if ((cookies = c.getRequestHeaders("Cookie")) != null
				&& cookies.length > 0 && parent == cookies[0]) {
			return getCookies(cookies[0].getValue());
		} else if (parent == getResponseHeaders(c)) {
			return getResponseHeaders(c);
		} else
			return new Object[0];
	}

	private String getRequestLine(Conversation c) {
		if (requestLine == null) {
			requestLine = c.getRequestMethod() + " " + c.getRequestUri() + " "
					+ c.getRequestVersion();
		}
		return requestLine;
	}

	private String getStatusLine(Conversation c) {
		if (statusLine == null) {
			statusLine = c.getResponseVersion() + " " + c.getResponseStatus()
					+ " " + c.getResponseMessage();
		}
		return statusLine;
	}

	private NamedValue[] getRequestHeaders(Conversation c) {
		if (requestHeaders == null) {
			requestHeaders = c.getRequestHeaders();
			if (requestHeaders == null)
				requestHeaders = NO_HEADERS;
		}
		return requestHeaders;
	}

	private NamedValue[] getResponseHeaders(Conversation c) {
		if (responseHeaders == null) {
			responseHeaders = c.getResponseHeaders();
			if (responseHeaders == null)
				responseHeaders = NO_HEADERS;
		}
		return responseHeaders;
	}

	private String[] getQueryParameters(String query) {
		if (queryParameters == null) {
			if (query == null) {
				queryParameters = new String[0];
			} else {
				queryParameters = query.split("&");
			}
		}
		return queryParameters;
	}

	private String[] getCookies(String cookie) {
		if (cookies == null) {
			if (cookie == null) {
				cookies = new String[0];
			} else {
				cookies = cookie.split(";\\s*");
			}
		}
		return cookies;
	}
}
