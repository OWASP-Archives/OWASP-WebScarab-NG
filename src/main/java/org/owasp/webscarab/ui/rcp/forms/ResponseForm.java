/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;

/**
 * @author rdawes
 * 
 */
public class ResponseForm extends AbstractForm {

	private static final String FORM_ID = "responseForm";

	private Form rawResponseForm;
	
	private Form parsedResponseForm;
	
	/**
	 * Constructor.
	 */
	public ResponseForm(FormModel model) {
		super(model, FORM_ID);
		rawResponseForm = new RawResponseForm(model);
		parsedResponseForm = new ParsedResponseForm(model);
	}

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Parsed", parsedResponseForm.getControl());
		tabbedPane.addTab("Raw", rawResponseForm.getControl());

		return tabbedPane;
	}

}
