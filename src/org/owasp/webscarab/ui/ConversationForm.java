/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.tree.AbstractTreeModel;

/**
 * @author rdawes
 * 
 */
public class ConversationForm extends AbstractForm {

	public ConversationForm(FormModel model) {
		super(model, "ConversationForm");
	}

	@Override
	protected JComponent createFormControl() {
		Conversation conversation = (Conversation) getFormObject();
		logger.error("Conversation is " + conversation);

		JTextArea requestTextArea = new JTextArea();
		ConversationTreeModel reqTreeModel = new ConversationTreeModel(conversation, ConversationTreeModel.REQUEST);
		JTree requestTree = new JTree(reqTreeModel);
		requestTree.setRootVisible(false);
		requestTree.setShowsRootHandles(true);
		requestTree.setEditable(true);
		ContentPanel requestContentPanel = new ContentPanel();
		requestContentPanel.setContentType(conversation.getRequestHeader("Content-Type"));
		requestContentPanel.setContent(conversation.getRequestContent());
		JSplitPane requestSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		requestSplitPane.setTopComponent(new JScrollPane(requestTree));
		requestSplitPane.setBottomComponent(requestContentPanel);
		requestSplitPane.setResizeWeight(0.1);
		JTabbedPane requestTabbedPane = new JTabbedPane();
		requestTabbedPane.addTab("Parsed", requestSplitPane);
		requestTabbedPane.addTab("Raw", requestTextArea);

		JTextArea responseTextArea = new JTextArea();
		ConversationTreeModel respTreeModel = new ConversationTreeModel(conversation, ConversationTreeModel.RESPONSE);
		JTree responseTree = new JTree(respTreeModel);
		responseTree.setRootVisible(false);
		responseTree.setShowsRootHandles(true);
		responseTree.setEditable(true);
		ContentPanel responseContentPanel = new ContentPanel();
		responseContentPanel.setContentType(conversation.getResponseHeader("Content-Type"));
		responseContentPanel.setContent(conversation.getResponseContent());
		JSplitPane responseSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		responseSplitPane.setTopComponent(new JScrollPane(responseTree));
		responseSplitPane.setBottomComponent(responseContentPanel);
		responseSplitPane.setResizeWeight(0.1);
		JTabbedPane responseTabbedPane = new JTabbedPane();
		responseTabbedPane.addTab("Parsed", responseSplitPane);
		responseTabbedPane.addTab("Raw", responseTextArea);

		JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setResizeWeight(0.5);
		mainSplitPane.setLeftComponent(requestTabbedPane);
		mainSplitPane.setRightComponent(responseTabbedPane);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainSplitPane);

		return panel;

		// GridBagLayoutFormBuilder formBuilder = new
		// GridBagLayoutFormBuilder(getBindingFactory());
		//
		// formBuilder.appendLabeledField("requestMethod");
		// // formBuilder.add("requestUrl");
		// formBuilder.appendLabeledField("requestVersion");
		// formBuilder.nextLine();
		// // formBuilder.add("requestContent");
		// // formBuilder.row();
		// formBuilder.appendLabeledField("responseVersion");
		// formBuilder.appendLabeledField("responseStatus");
		// formBuilder.appendLabeledField("responseMessage");
		// // formBuilder.row();
		// // formBuilder.add("responseContent");
		// return formBuilder.getPanel();
		// // return new JLabel("Form control");
	}

	private class ConversationTreeModel extends AbstractTreeModel {

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
			logger.info("Path " + path + " changed to " + newValue);
			// TODO Auto-generated method stub

		}

		private Object[] getChildren(Object parent) {
			Conversation c = (Conversation) getRoot();
			NamedValue[] cookies;
			if (parent == c) {
				return new Object[] {
						which == REQUEST ? getRequestLine(c) : getStatusLine(c),
						which == REQUEST ? getRequestHeaders(c) : getResponseHeaders(c) };
			} else if (parent == getRequestLine(c)) {
				URL url = c.getRequestUrl();
				return getQueryParameters(url.getQuery());
			} else if (parent == getRequestHeaders(c)) {
				return getRequestHeaders(c);
			} else if ((cookies = c.getRequestHeaders("Cookie")) != null && cookies.length > 0 && parent == cookies[0]) {
				return getCookies(cookies[0].getValue());
			} else if (parent == getResponseHeaders(c)) {
				return getResponseHeaders(c);
			} else
				return new Object[0];
		}

		private String getRequestLine(Conversation c) {
			if (requestLine == null) {
				requestLine = c.getRequestMethod() + " " + c.getRequestUrl()
						+ " " + c.getRequestVersion();
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

}
