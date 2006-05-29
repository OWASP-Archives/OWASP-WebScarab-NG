/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 * 
 */
public class RequestForm extends AbstractForm {

	private static final String FORM_ID = "requestForm";
	
	private RawRequestForm rawRequestForm;
	
	/**
	 * Constructor.
	 */
	public RequestForm(FormModel model) {
		super(model, FORM_ID);
		rawRequestForm = new RawRequestForm(model);
		addChildForm(rawRequestForm);
	}

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		JTabbedPane tabbedPane = new JTabbedPane();
//		tabbedPane.addTab("Parsed", parsedRequestForm.createFormControl());
		tabbedPane.addTab("Raw", rawRequestForm.createFormControl());

		return tabbedPane;
	}

}
