/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.owasp.webscarab.Conversation;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 * 
 */
public class ConversationView extends AbstractView {

	private Form form = null;

	private Conversation conversation = null;

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
		if (form != null) {
			form.setFormObject(conversation);
		}
	}

	public Conversation getConversation() {
		if (this.conversation == null) 
			return new Conversation();
		return this.conversation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#componentClosed()
	 */
	@Override
	public void componentClosed() {
		logger.info("component Closed");
		super.componentClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@Override
	protected JComponent createControl() {
		final Conversation conversation = getConversation();
		 form = new ConversationForm(FormModelHelper
//		form = new RequestForm(FormModelHelper
				.createFormModel(conversation));
		JPanel panel = getComponentFactory().createPanel();
		panel.setLayout(new BorderLayout());
		panel.add(form.getControl());
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#registerLocalCommandExecutors(org.springframework.richclient.application.PageComponentContext)
	 */
	@Override
	protected void registerLocalCommandExecutors(PageComponentContext context) {
		// TODO Auto-generated method stub
		super.registerLocalCommandExecutors(context);
	}

}
