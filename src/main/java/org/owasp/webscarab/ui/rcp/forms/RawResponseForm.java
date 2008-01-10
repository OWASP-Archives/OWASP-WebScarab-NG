/**
 *
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.util.CharsetUtils;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 *
 */
public class RawResponseForm extends AbstractForm {

	private static final String FORM_ID = "rawResponse";

	private static final String[] properties = {
		Conversation.PROPERTY_RESPONSE_VERSION,
		Conversation.PROPERTY_RESPONSE_STATUS,
		Conversation.PROPERTY_RESPONSE_MESSAGE,
		Conversation.PROPERTY_RESPONSE_HEADERS,
		Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT };

	private JTextArea textArea;

	private JScrollPane scrollPane;

	private Listener listener = new Listener();

	private String charset;

	private boolean updating = false;

	private boolean readOnly;

	public RawResponseForm(FormModel formModel) {
		super(formModel, FORM_ID);
		readOnly = false;
		for (int i = 0; i < properties.length; i++) {
			readOnly |= formModel.getFieldMetadata(properties[i]).isReadOnly();
			getValueModel(properties[i]).addValueChangeListener(listener);
		}
	}

	@Override
	protected JComponent createFormControl() {
		if (scrollPane == null) {
			textArea = getComponentFactory().createTextArea();
			textArea.setText(responseString());
			textArea.setEditable(!readOnly);
			textArea.getDocument().addDocumentListener(listener);
			scrollPane = getComponentFactory().createScrollPane(textArea);
			scrollPane.addComponentListener(listener);
		}
		return scrollPane;
	}

	private String responseString() {
		StringBuilder b = new StringBuilder();
		ValueModel vm;
		vm = getValueModel(Conversation.PROPERTY_RESPONSE_VERSION);
        if (vm.getValue() != null)
            b.append(vm.getValue()).append(" ");
		vm = getValueModel(Conversation.PROPERTY_RESPONSE_STATUS);
        if (vm.getValue() != null)
            b.append(vm.getValue()).append(" ");
		vm = getValueModel(Conversation.PROPERTY_RESPONSE_MESSAGE);
        if (vm.getValue() != null)
            b.append(vm.getValue()).append("\n");
		vm = getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS);
		NamedValue[] headers = (NamedValue[]) vm.getValue();
		if (headers != null)
			for (int i = 0; i < headers.length; i++) {
				b.append(headers[i].getName()).append(": ");
				b.append(headers[i].getValue()).append("\n");
			}
		b.append("\n");
		vm = getValueModel(Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT);
		byte[] content = (byte[]) vm.getValue();
		if (content != null) {
			charset = CharsetUtils.getCharset(content);
			if (charset == null) {
				b.append(new String(content));
			} else {
				try {
					b.append(new String(content, charset));
				} catch (UnsupportedEncodingException uee) {
					b.append(new String(content));
				}
			}
		}
		return b.toString();
	}

	private void parseResponse() {
		String response = textArea.getText();
		String responseLine = null;
		String[] headerLines = null;
		String contentString = null;
		// we break the response up into 3 major Strings
		// the response line, the headerLines and the content
		int cr = response.indexOf("\n");
		if (cr < 0) {
			responseLine = response;
		} else {
			responseLine = response.substring(0, cr);
		}
		if (cr > 0) {
			int blank = response.indexOf("\n\n", cr);
			if (blank > cr + 1) {
				headerLines = response.substring(cr + 1, blank).split("\n");
				if (blank + 2 < response.length()) {
					contentString = response.substring(blank + 2);
				}
			} else if (cr + 1 < response.length()) {
				headerLines = response.substring(cr + 1).split("\n");
			}
		}

		// Now we split the response line into its component parts
		String[] parts = responseLine.split(" ", 3);

		ValueModel vm = getValueModel(Conversation.PROPERTY_RESPONSE_VERSION);
		if (parts.length > 0 && parts[0] != null && parts[0].length() != 0) {
			vm.setValueSilently(parts[0], listener);
		} else {
			vm.setValueSilently(null, listener);
		}

		vm = getValueModel(Conversation.PROPERTY_RESPONSE_STATUS);
		if (parts.length > 1 && parts[1] != null && parts[1].length() != 0) {
			vm.setValueSilently(parts[1], listener);
		} else {
			vm.setValueSilently(null, listener);
		}

		vm = getValueModel(Conversation.PROPERTY_RESPONSE_MESSAGE);
		if (parts.length > 2 && parts[2] != null && parts[2].length() != 0) {
			vm.setValueSilently(parts[2], listener);
		} else {
			vm.setValueSilently(null, listener);
		}

		vm = getValueModel(Conversation.PROPERTY_RESPONSE_HEADERS);
		if (headerLines != null && headerLines.length > 0) {
			List<NamedValue> list = new ArrayList<NamedValue>();
			for (int i = 0; i < headerLines.length; i++) {
				parts = headerLines[i].split(":\\s*", 2);
				if (parts.length == 2)
					list.add(new NamedValue(parts[0], parts[1]));
			}
			NamedValue[] headers = null;
			if (list.size() > 0)
				headers = list.toArray(new NamedValue[list.size()]);
			vm.setValueSilently(headers, listener);
		} else {
			vm.setValueSilently(null, listener);
		}

		vm = getValueModel(Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT);
		if (contentString != null && contentString.length() > 0) {
			if (charset == null) {
				vm.setValueSilently(contentString.getBytes(), listener);
			} else {
				try {
					vm.setValueSilently(contentString.getBytes(charset), listener);
				} catch (UnsupportedEncodingException uee) {
					vm.setValueSilently(contentString.getBytes(), listener);
				}
			}
		} else {
			vm.setValueSilently(null, listener);
		}
	}

	private void updateFormControl() {
		updating = true;
		textArea.setText(responseString());
		textArea.setCaretPosition(0);
		updating = false;
	}

	private class Listener extends ComponentAdapter implements
			PropertyChangeListener, DocumentListener {

		private boolean upToDate = false;

		public void propertyChange(PropertyChangeEvent evt) {
			// this event should only fire when the conversation changes
			// externally
			// not by means of typing in the textArea
			// in that case, it makes sense to reset the caret position
			// we also have to flag the update, so that we don't try to reparse
			// the text area unnecessarily, when the change is external
		    upToDate = false;
			if (textArea != null && textArea.isShowing()) {
				updateFormControl();
				upToDate = true;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			if (!updating)
				parseResponse();
		}

		public void insertUpdate(DocumentEvent e) {
			if (!updating)
				parseResponse();
		}

		public void removeUpdate(DocumentEvent e) {
			if (!updating)
				parseResponse();
		}

		public void componentShown(ComponentEvent e) {
			if (!upToDate) {
				updateFormControl();
				upToDate = true;
			}
		}

	}

}
