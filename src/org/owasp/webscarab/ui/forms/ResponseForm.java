/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class ResponseForm extends AbstractForm {

	private static final String FORM_ID = "responseForm";

	private JTree tree;

	private ContentPanel contentPanel;

	private JTextArea textArea;

	/**
	 * Constructor.
	 */
	public ResponseForm(FormModel model) {
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
			if (tree != null) {
				ConversationTreeModel treeModel = new ConversationTreeModel(
						getConversation(), ConversationTreeModel.RESPONSE);
				tree.setModel(treeModel);
			}
			if (contentPanel != null) {
				contentPanel.setContentType(getConversation()
						.getResponseHeader("Content-Type"));
				contentPanel.setContent(getConversation().getResponseContent());
			}
			if (textArea != null) {
				StringBuffer buff = new StringBuffer();
				buff.append(getConversation().getResponseVersion()).append(" ");
				buff.append(getConversation().getResponseStatus()).append(" ");
				buff.append(getConversation().getResponseMessage())
						.append("\n");
				NamedValue[] headers = getConversation().getResponseHeaders();
				if (headers != null) {
					for (int i = 0; i < headers.length; i++) {
						buff.append(headers[i].getName()).append(": ");
						buff.append(headers[i].getValue()).append("\n");
					}
				}
				buff.append("\n");
				if (getConversation().getResponseContent() != null) {
					buff.append(new String(getConversation()
							.getResponseContent()));
				}
				textArea.setText(buff.toString());
			}
		}
	}
}
