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
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class RequestForm extends AbstractForm {

	private static final String FORM_ID = "requestForm";

	private JTree tree;

	private ContentPanel contentPanel;

	private JTextArea textArea;

	/**
	 * Constructor.
	 */
	public RequestForm(FormModel model) {
		super(model, FORM_ID);
		addFormObjectChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refresh();
			}
		});
	}

	public void setConversation(Conversation conversation) {
		setFormObject(conversation);
	}

	public Conversation getConversation() {
		return (Conversation) getFormObject();
	}

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		textArea = new JTextArea();
		tree = new JTree();
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setEditable(getFormModel().isEnabled());
		contentPanel = new ContentPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(new JScrollPane(tree));
		splitPane.setBottomComponent(contentPanel);
		splitPane.setResizeWeight(0.5);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Parsed", splitPane);
		tabbedPane.addTab("Raw", new JScrollPane(textArea));

		refresh();

		return tabbedPane;
	}

	private void refresh() {
		if (getConversation() != null) {
			ConversationTreeModel treeModel = new ConversationTreeModel(
					getConversation(), ConversationTreeModel.REQUEST);
			tree.setModel(treeModel);
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
			textArea.setText(buff.toString());
		}
	}
}
