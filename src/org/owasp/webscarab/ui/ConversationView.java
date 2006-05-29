/**
 * 
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.form.Form;

/**
 * @author rdawes
 * 
 */
public class ConversationView extends AbstractView {

	private Form requestForm;
	private Form responseForm;
	private Form annotationForm;
	
	public ConversationView(Form requestForm, Form responseForm, Form annotationForm) {
		this.requestForm = requestForm;
		this.responseForm = responseForm;
		this.annotationForm = annotationForm;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@Override
	protected JComponent createControl() {
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setResizeWeight(0.5);
		splitPane.setTopComponent(requestForm.getControl());
		splitPane.setBottomComponent(responseForm.getControl());
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(splitPane, BorderLayout.CENTER);
		if (annotationForm != null)
			panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
		return panel;
	}

}
