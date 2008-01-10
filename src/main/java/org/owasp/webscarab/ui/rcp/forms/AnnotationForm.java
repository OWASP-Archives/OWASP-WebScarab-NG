/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.owasp.webscarab.domain.Annotation;
import org.springframework.binding.form.FormModel;
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
		setFormModel(FormModelHelper.createUnbufferedFormModel(annotation));
	}

	public AnnotationForm(FormModel model) {
		super(model, FORM_ID);
	}

	@Override
	protected JComponent createFormControl() {
		TableFormBuilder formBuilder = new TableFormBuilder(getBindingFactory());
		formBuilder.add(Annotation.PROPERTY_ANNOTATION);
		JComponent form = formBuilder.getForm();
		form.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "commit");
		form.getActionMap().put("commit", new AbstractAction() {
			private static final long serialVersionUID = 5557648408959460263L;
			public void actionPerformed(ActionEvent e) {
				getFormModel().commit();
			}
		});
		return form;
	}
}
