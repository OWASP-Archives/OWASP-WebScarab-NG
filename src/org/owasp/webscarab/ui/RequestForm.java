/**
 * 
 */
package org.owasp.webscarab.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 * 
 */
public class RequestForm extends AbstractForm {

	private static final String FORM_ID = "requestForm";

	private JTree requestTree;

	private ContentPanel contentPanel;

	private JTextArea requestTextArea;

	/**
	 * Constructor.
	 */
	public RequestForm(FormModel model) {
		super(model, FORM_ID);
		setFormModel(FormModelHelper
				.createUnbufferedFormModel(new Conversation()));

		addFormObjectChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refresh();
			}
		});
	}

	public void setConversation(Conversation conversation) {
		logger.info("SetConversation call with " + conversation);
		setFormObject(conversation);
	}

	public Conversation getConversation() {
		return (Conversation) getFormObject();
	}

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		requestTextArea = new JTextArea();
		requestTree = new JTree();
		requestTree.setRootVisible(false);
		requestTree.setShowsRootHandles(true);
		requestTree.setEditable(getFormModel().isEnabled());
		contentPanel = new ContentPanel();
		JSplitPane requestSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		requestSplitPane.setTopComponent(new JScrollPane(requestTree));
		requestSplitPane.setBottomComponent(contentPanel);
		requestSplitPane.setResizeWeight(0.1);
		JTabbedPane requestTabbedPane = new JTabbedPane();
		requestTabbedPane.addTab("Parsed", requestSplitPane);
		requestTabbedPane.addTab("Raw", new JScrollPane(requestTextArea));

		refresh();

		return requestTabbedPane;
	}

	private void refresh() {
		if (getConversation() != null) {
			ConversationTreeModel treeModel = new ConversationTreeModel(
					getConversation(), ConversationTreeModel.REQUEST);
			requestTree.setModel(treeModel);
			contentPanel.setContentType(getConversation().getRequestHeader(
					"Content-Type"));
			contentPanel.setContent(getConversation().getRequestContent());

			StringBuffer buff = new StringBuffer();
			buff.append(getConversation().getRequestMethod()).append(" ");
			buff.append(getConversation().getRequestUrl()).append(" ");
			buff.append(getConversation().getRequestVersion()).append("\n");
			NamedValue[] headers = getConversation().getRequestHeaders();
			if (headers != null) {
				for (int i = 0; i < headers.length; i++) {
					buff.append(headers[i].getName()).append(": ");
					buff.append(headers[i].getValue()).append("\n");
				}
			}
			buff.append("\n");
			if (getConversation().getRequestContent() != null) {
				buff.append(new String(getConversation().getRequestContent()));
			}
			requestTextArea.setText(buff.toString());
		}
	}
}
