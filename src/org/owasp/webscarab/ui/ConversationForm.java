/**
 * 
 */
package org.owasp.webscarab.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.owasp.webscarab.Conversation;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class ConversationForm extends AbstractForm {

	private final RequestForm requestForm;
	private final ResponseForm responseForm;
	
	public ConversationForm(FormModel model) {
		super(model, "ConversationForm");
		requestForm = new RequestForm(model);
		responseForm = new ResponseForm(model);
		addFormObjectChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				requestForm.setConversation((Conversation)evt.getNewValue());
				responseForm.setConversation((Conversation)evt.getNewValue());
			}
		});
	}

	@Override
	protected JComponent createFormControl() {
		JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setResizeWeight(0.5);
		mainSplitPane.setLeftComponent(requestForm.getControl());
		mainSplitPane.setRightComponent(responseForm.getControl());
		return mainSplitPane;
	}

}
