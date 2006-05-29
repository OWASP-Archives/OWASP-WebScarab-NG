/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.owasp.webscarab.Annotation;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author rdawes
 *
 */
public class AnnotationForm extends AbstractForm {

	private static final String FORM_ID = "annotationForm";
	
	public AnnotationForm() {
		super(FORM_ID);
		Annotation annotation = new Annotation();
		setFormModel( FormModelHelper.createUnbufferedFormModel(annotation));
	}
	
	@Override
	protected JComponent createFormControl() {
        TableFormBuilder formBuilder = new TableFormBuilder( getBindingFactory() );
        JComponent component = formBuilder.add( Annotation.PROPERTY_ANNOTATION)[1];
        component.setPreferredSize(new Dimension(200, (int)component.getPreferredSize().getHeight()));
        component.setMinimumSize(component.getPreferredSize());
		return formBuilder.getForm();
	}

}
