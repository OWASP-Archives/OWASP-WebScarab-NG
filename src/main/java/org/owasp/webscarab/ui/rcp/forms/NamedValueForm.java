/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import javax.swing.JComponent;

import org.owasp.webscarab.domain.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author rdawes
 *
 */
public class NamedValueForm extends AbstractForm {

	private static final String FORM_ID = "namedValueForm";

	private JComponent nameField;
	private JComponent valueField;
	
	public NamedValueForm() {
		this(FormModelHelper.createFormModel(new NamedValue(null, null)));
	}
	
	/**
	 * Constructor.
	 */
	public NamedValueForm(FormModel model) {
		super(model, FORM_ID);
	}

    /**
     * Construct the form with the required fields.
     */
    protected JComponent createFormControl() {
        TableFormBuilder formBuilder = new TableFormBuilder( getBindingFactory() );
        nameField = formBuilder.add(NamedValue.PROPERTY_NAME)[1];
        formBuilder.row();
        valueField = formBuilder.add(NamedValue.PROPERTY_VALUE)[1];
        return formBuilder.getForm();
    }

    public boolean requestFocusInWindow() {
        // Put the focus on the right field
    	String name = ((NamedValue)getFormObject()).getName();
        JComponent field = null;
        if (name == null || name.length() == 0) {
        	field = nameField;
        } else {
        	field = valueField;
        }
        return field.requestFocusInWindow();
    }
    
}
