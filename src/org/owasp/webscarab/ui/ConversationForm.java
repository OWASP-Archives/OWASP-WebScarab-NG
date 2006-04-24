/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.owasp.webscarab.Conversation;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class ConversationForm extends AbstractForm {

	private final RequestForm requestForm;
	
	public ConversationForm(FormModel model) {
		super(model, "ConversationForm");
		requestForm = new RequestForm(model);
		addFormObjectChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				requestForm.setConversation((Conversation)evt.getNewValue());
			}
		});
	}

	@Override
	protected JComponent createFormControl() {
		Conversation conversation = (Conversation) getFormObject();

		JTextArea responseTextArea = new JTextArea();
		ConversationTreeModel respTreeModel = new ConversationTreeModel(conversation, ConversationTreeModel.RESPONSE);
		JTree responseTree = new JTree(respTreeModel);
		responseTree.setRootVisible(false);
		responseTree.setShowsRootHandles(true);
		responseTree.setEditable(getFormModel().isEnabled());
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
		mainSplitPane.setLeftComponent(requestForm.getControl());
		mainSplitPane.setRightComponent(responseTabbedPane);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainSplitPane);

		return panel;
	}


}
