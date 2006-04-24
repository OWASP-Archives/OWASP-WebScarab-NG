/**
 * 
 */
package org.owasp.webscarab.ui;

import javax.swing.JComponent;

import org.owasp.webscarab.Conversation;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 * 
 */
public class ConversationView extends AbstractView {

	private Form form = null;
	
	public void setConversation(Conversation conversation) {
		if (form == null) {
			createForm();
		}
		form.setFormObject(conversation);
	}

	public Conversation getConversation() {
		return (Conversation) form.getFormObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@Override
	protected JComponent createControl() {
		if (form == null) {
			createForm();
		}
		return form.getControl();
	}

	private void createForm() {
		form = new ConversationForm(FormModelHelper
				.createFormModel(new Conversation()));
	}
}
