/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 *
 */
public class TextForm extends AbstractForm {

	private static String FORM_ID = "textForm";
	
	private ContentListener listener;
	
	private ValueModel vm;
	
	private JTextArea textArea;
	
	public TextForm(FormModel model, String propertyName) {
		super(model, FORM_ID);
		listener = new ContentListener();
		model.addPropertyChangeListener(listener);
		vm = model.getValueModel(propertyName);
		vm.addValueChangeListener(listener);
	}

	@Override
	protected JComponent createFormControl() {
		textArea = getComponentFactory().createTextArea();
		textArea.setEditable(getFormModel().isEnabled());
		textArea.setText(contentString());
		textArea.getDocument().addDocumentListener(listener);
		return new JScrollPane(textArea);
	}

	private String contentString() {
		byte[] content = (byte[]) vm.getValue();
		if (content == null) return null;
		return new String(content);
	}
	
	private void updateContent() {
		String content = textArea.getText();
		if (content == null || content.length() == 0) {
			vm.setValueSilently(null, listener);
		} else {
			vm.setValueSilently(content.getBytes(), listener);
		}
	}
	
	private class ContentListener implements PropertyChangeListener, DocumentListener {

		private boolean updating = false;
		
		public void propertyChange(PropertyChangeEvent evt) {
			// if we have not yet constructed the text area, we don't care
			if (textArea == null) 
				return;
			// this event should only fire when the content changes externally
			// not by means of typing in the textArea
			// in that case, it makes sense to reset the caret position
			// we also have to flag the update, so that we don't try to reparse
			// the text area unnecessarily, when the change is external
			// Alternatively, it could fire if the FormModel itself changes settings
			if (evt.getSource() == getFormModel()) {
				if (evt.getPropertyName().equals(ValidatingFormModel.ENABLED_PROPERTY)) 
					textArea.setEnabled(getFormModel().isEnabled());
			} else if (!updating) {
				// we'd like to test if the evt.getSource is the ValueModel
				// but the event is actually fired by a wrapped class, so
				// that doesn't work!
				updating = true;
				textArea.setText(contentString());
				textArea.setCaretPosition(0);
				updating = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			if (!updating)
				updateContent();
		}

		public void insertUpdate(DocumentEvent e) {
			if (!updating)
				updateContent();
		}

		public void removeUpdate(DocumentEvent e) {
			if (!updating)
				updateContent();
		}
		
	}
	
}
