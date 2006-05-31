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
	
	public Form getAnnotationForm() {
		return this.annotationForm;
	}

	public void setAnnotationForm(Form annotationForm) {
		this.annotationForm = annotationForm;
	}

	public Form getRequestForm() {
		return this.requestForm;
	}

	public void setRequestForm(Form requestForm) {
		this.requestForm = requestForm;
	}

	public Form getResponseForm() {
		return this.responseForm;
	}

	public void setResponseForm(Form responseForm) {
		this.responseForm = responseForm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@Override
	protected JComponent createControl() {
		JSplitPane splitPane = null;
		JPanel panel = new JPanel(new BorderLayout());
		if (responseForm != null) {
			splitPane = new JSplitPane();
			splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setResizeWeight(0.5);
			splitPane.setTopComponent(requestForm.getControl());
			splitPane.setBottomComponent(responseForm.getControl());
			panel.add(splitPane, BorderLayout.CENTER);
		} else {
			panel.add(requestForm.getControl(), BorderLayout.CENTER);
		}
		if (annotationForm != null)
			panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
		return panel;
	}

}
