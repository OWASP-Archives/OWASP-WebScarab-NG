/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.Color;
import java.io.UnsupportedEncodingException;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.springframework.binding.form.FormModel;

/**
 * @author rdawes
 * 
 */
public class TextForm extends AbstractContentForm {

	private static String FORM_ID = "textForm";

	private ContentListener listener;

	private JTextArea textArea;

	private Color normalColor, errorColor = Color.PINK;

	public TextForm(FormModel model, String headerPropertyName,
			String contentPropertyName) {
		super(model, FORM_ID, headerPropertyName, contentPropertyName);
	}

	@Override
	protected JComponent createContentFormControl() {
		listener = new ContentListener();
		textArea = getComponentFactory().createTextArea();
		normalColor = textArea.getBackground();
		textArea.setEditable(!isReadOnly());
		updateContentFormControl();
		textArea.getDocument().addDocumentListener(listener);
		return getComponentFactory().createScrollPane(textArea);
	}

	protected void clearContentFormControl() {
		textArea.setText("");
		textArea.setCaretPosition(0);
		textArea.setBackground(normalColor);
	}

	protected void updateContentFormControl() {
	    try {
    		textArea.setText(getContentAsString());
    		textArea.setCaretPosition(0);
    		textArea.setBackground(normalColor);
	    } catch (UnsupportedEncodingException uee) {
	        textArea.setText("");
            textArea.setBackground(errorColor);
	    }
	}

	public boolean canHandle(String contentType) {
		if (contentType == null)
			return false;
		if (contentType.matches("text/.*"))
			return true;
		if (contentType.matches("application/x-javascript"))
			return true;
		if (contentType.matches("application/x-www-form-urlencoded"))
			return true;
		return false;
	}

	private void parseChanges() {
		String content = textArea.getText();
		try {
			setContent(content);
			textArea.setBackground(normalColor);
		} catch (UnsupportedEncodingException uee) {
			textArea.setBackground(errorColor);
		}
	}

	private class ContentListener implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
			if (!isUpdating())
				parseChanges();
		}

		public void insertUpdate(DocumentEvent e) {
			if (!isUpdating())
				parseChanges();
		}

		public void removeUpdate(DocumentEvent e) {
			if (!isUpdating())
				parseChanges();
		}

	}

}
