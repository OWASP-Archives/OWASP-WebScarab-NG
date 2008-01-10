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
public class RequestForm extends AbstractForm {

	private static final String FORM_ID = "requestForm";

	private Form rawRequestForm;

	private Form parsedRequestForm;

	/**
	 * Constructor.
	 */
	public RequestForm(FormModel model) {
		super(model, FORM_ID);
		rawRequestForm = new RawRequestForm(model);
		parsedRequestForm = new ParsedRequestForm(model);
	}

	/**
	 * Construct the form with the required fields.
	 */
	protected JComponent createFormControl() {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Parsed", parsedRequestForm.getControl());
		tabbedPane.addTab("Raw", rawRequestForm.getControl());

		return tabbedPane;
	}

    /* (non-Javadoc)
     * @see org.springframework.richclient.form.AbstractForm#getRevertCommandFaceDescriptorId()
     */
    @Override
    protected String getRevertCommandFaceDescriptorId() {
        return "revertCommand";
    }

}
