/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.NamedValue;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * @author rdawes
 *
 */
public class RawRequestForm extends AbstractForm {

	private static final String FORM_ID = "rawRequest";
	
	private JTextArea textArea;
	
	private JScrollPane scrollPane;

	private Listener listener = new Listener();
	
	public RawRequestForm(FormModel formModel) {
		super(formModel, FORM_ID);
		formModel.getValueModel(Conversation.PROPERTY_REQUEST_METHOD).addValueChangeListener(listener);
		formModel.getValueModel(Conversation.PROPERTY_REQUEST_URI).addValueChangeListener(listener);
		formModel.getValueModel(Conversation.PROPERTY_REQUEST_VERSION).addValueChangeListener(listener);
		formModel.getValueModel(Conversation.PROPERTY_REQUEST_HEADERS).addValueChangeListener(listener);
		formModel.getValueModel(Conversation.PROPERTY_REQUEST_CONTENT).addValueChangeListener(listener);
	}

	@Override
	protected JComponent createFormControl() {
		if (scrollPane == null) {
			textArea = getComponentFactory().createTextArea();
			textArea.setText(requestString());
			textArea.getDocument().addDocumentListener(listener);
			scrollPane = getComponentFactory().createScrollPane(textArea);
		}
		return scrollPane;
	}
	
	private String requestString() {
		StringBuilder b = new StringBuilder();
		b.append(getValueModel(Conversation.PROPERTY_REQUEST_METHOD).getValue()).append(" ");
		b.append(getValueModel(Conversation.PROPERTY_REQUEST_URI).getValue()).append(" ");
		b.append(getValueModel(Conversation.PROPERTY_REQUEST_VERSION).getValue()).append("\n");
		NamedValue[] headers = (NamedValue[]) getValueModel(Conversation.PROPERTY_REQUEST_HEADERS).getValue();
		if (headers != null)
			for (int i=0; i<headers.length; i++) {
				b.append(headers[i].getName()).append(": ");
				b.append(headers[i].getValue()).append("\n");
			}
		b.append("\n");
		byte[] content = (byte[]) getValueModel(Conversation.PROPERTY_REQUEST_CONTENT).getValue();
		if (content != null)
			b.append(new String(content));
		return b.toString();
	}

	private void parseRequest() {
		String request = textArea.getText();
		String requestLine = null;
		String[] headerLines = null;
		String contentString = null;
		// we break the request up into 3 major Strings
		// the request line, the headerLines and the content
		int cr = request.indexOf("\n");
		if (cr <0) {
			requestLine = request;
		} else {
			requestLine = request.substring(0, cr);
		}
		if (cr > 0) {
			int blank = request.indexOf("\n\n", cr);
			if (blank > cr + 1) {
				headerLines = request.substring(cr+1, blank).split("\n");
				if (blank + 2 < request.length()) {
					contentString = request.substring(blank+2);
				}
			} else if (cr+1<request.length()) {
				headerLines = request.substring(cr+1).split("\n");
			}
		}
		
		// Now we split the request line into its component parts
		String[] parts = requestLine.split(" ");
		
		ValueModel vm = getValueModel(Conversation.PROPERTY_REQUEST_METHOD);
		if (parts.length>0 && parts[0] != null && parts[0].length() != 0) {
			vm.setValueSilently(parts[0], listener);
		} else {
			vm.setValueSilently(null, listener);
		}
		
		vm = getValueModel(Conversation.PROPERTY_REQUEST_URI);
		if (parts.length>1 && parts[1] != null && parts[1].length() != 0) {
			try {
				vm.setValueSilently(new URI(parts[1]), listener);
			} catch (URISyntaxException use) {
				vm.setValueSilently(null, listener);
			}
		} else {
			vm.setValueSilently(null, listener);
		}
		
		vm = getValueModel(Conversation.PROPERTY_REQUEST_VERSION);
		if (parts.length>2 && parts[2] != null && parts[2].length() != 0) {
			vm.setValueSilently(parts[2], listener);
		} else {
			vm.setValueSilently(null, listener);
		}
		if (parts.length>3) {
			System.out.println("Too many parts on the request line: " + parts.length);
		}
		
		vm = getValueModel(Conversation.PROPERTY_REQUEST_HEADERS);
		if (headerLines != null && headerLines.length > 0) {
			List<NamedValue> list = new ArrayList<NamedValue>();
			for (int i=0; i< headerLines.length; i++) {
				parts = headerLines[i].split(":\\s*", 2);
				if (parts.length == 2)
					list.add(new NamedValue(parts[0], parts[1]));
			}
			NamedValue[] headers = null;
			if (list.size()>0)
				headers = list.toArray(new NamedValue[list.size()]);
			vm.setValueSilently(headers, listener);
		} else {
			vm.setValueSilently(null, listener);
		}
		
		vm = getValueModel(Conversation.PROPERTY_REQUEST_CONTENT);
		if (contentString != null && contentString.length()>0) {
			vm.setValueSilently(contentString.getBytes(), listener);
		} else {
			vm.setValueSilently(null, listener);
		}
	}
	
	private class Listener implements PropertyChangeListener, DocumentListener {

		private boolean updating = false;
		
		public void propertyChange(PropertyChangeEvent evt) {
			// this event should only fire when the conversation changes externally
			// not by means of typing in the textArea
			// in that case, it makes sense to reset the caret position
			// we also have to flag the update, so that we don't try to reparse
			// the text area unnecessarily, when the change is external
			if (textArea != null) {
				updating = true;
				textArea.setText(requestString());
				textArea.setCaretPosition(0);
				updating = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			if (!updating)
				parseRequest();
		}

		public void insertUpdate(DocumentEvent e) {
			if (!updating)
				parseRequest();
		}

		public void removeUpdate(DocumentEvent e) {
			if (!updating)
				parseRequest();
		}
		
	}
	
}
