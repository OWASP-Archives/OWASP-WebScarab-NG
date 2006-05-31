/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 *
 */
public class HexForm extends AbstractForm {

	private static String FORM_ID = "hexForm";
	
	private String propertyName;
	
	public HexForm(FormModel model, String propertyName) {
		super(model, FORM_ID);
		this.propertyName = propertyName;
	}

	@Override
	protected JComponent createFormControl() {
		ValueModel vm = getValueModel(propertyName);
		return new JScrollPane(new HexTable(vm, getFormModel().isEnabled()));
	}
	
}
