/**
 * 
 */
package org.owasp.webscarab.util.swing;

import javax.swing.JComponent;

import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

/**
 * @author rdawes
 *
 */
public class FormView extends AbstractView {

	private Form form;
	
	public FormView(Form form) {
		Assert.notNull(form, "Form may not be null");
		this.form = form;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@Override
	protected JComponent createControl() {
		return form.getControl();
	}
	
}
