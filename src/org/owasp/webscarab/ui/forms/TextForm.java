/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 *
 */
public class TextForm extends AbstractForm implements ContentForm {

	private static String FORM_ID = "textForm";
	
	private ContentListener listener;
	
	private ValueModel vm;
	
	private JTextArea textArea;
	
	private JScrollPane scrollPane = null;
	
	private boolean updating = false;
	
	private String propertyName;
	
	public TextForm(FormModel model, String propertyName) {
		super(model, FORM_ID);
		this.propertyName = propertyName;
		vm = model.getValueModel(propertyName);
	}

	@Override
	protected JComponent createFormControl() {
		if (scrollPane == null) {
			listener = new ContentListener();
			vm.addValueChangeListener(listener);
			textArea = getComponentFactory().createTextArea();
			textArea.setEditable(! getFormModel().getFieldMetadata(propertyName).isReadOnly());
			textArea.setText(contentString());
			textArea.getDocument().addDocumentListener(listener);
			scrollPane = getComponentFactory().createScrollPane(textArea);
			scrollPane.addComponentListener(listener);
		}
		return scrollPane;
	}

	private void updateFormControl() {
		updating = true;
		textArea.setText(contentString());
		textArea.setCaretPosition(0);
		updating = false;
	}
	
	public boolean canHandle(String contentType) {
		if (contentType == null) return false;
		if (contentType.matches("text/.*")) return true;
		if (contentType.matches("application/x-javascript")) return true;
		if (contentType.matches("application/x-www-form-urlencoded")) return true;
		return false;
	}
	
	private String contentString() {
		byte[] content = (byte[]) vm.getValue();
		if (content == null) return null;
		return new String(content);
	}
	
	private void parseChanges() {
		String content = textArea.getText();
		if (content == null || content.length() == 0) {
			vm.setValueSilently(null, listener);
		} else {
			vm.setValueSilently(content.getBytes(), listener);
		}
	}
	
	private class ContentListener extends ComponentAdapter implements PropertyChangeListener, DocumentListener {

		private boolean upToDate = false;
		
		public void propertyChange(PropertyChangeEvent evt) {
			// if we have not yet constructed the text area, we don't care
			if (textArea == null) 
				return;
			// this event should only fire when the content changes externally
			// not by means of typing in the textArea
			// in that case, it makes sense to reset the caret position
			// we also have to flag the update, so that we don't try to reparse
			// the text area unnecessarily, when the change is external
			upToDate = false;
			if (textArea != null && textArea.isShowing()) {
				// we'd like to test if the evt.getSource is the ValueModel
				// but the event is actually fired by a wrapped class, so
				// that doesn't work!
				updateFormControl();
				upToDate = true;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			if (!updating)
				parseChanges();
		}

		public void insertUpdate(DocumentEvent e) {
			if (!updating)
				parseChanges();
		}

		public void removeUpdate(DocumentEvent e) {
			if (!updating)
				parseChanges();
		}
		
		public void componentShown(ComponentEvent e) {
			if (!upToDate) {
				updateFormControl();
				upToDate = true;
			}
		}

		
	}
	
}
