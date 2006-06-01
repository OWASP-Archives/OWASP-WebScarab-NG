/**
 * 
 */
package org.owasp.webscarab.ui.forms;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.owasp.webscarab.domain.Conversation;
import org.springframework.binding.form.FormModel;

/**
 * @author rdawes
 * 
 */
public class ParsedRequestForm extends AbstractParsedContentForm {

	private static final String FORM_ID = "parsedRequestForm";
	
	public ParsedRequestForm(FormModel model, boolean editable) {
		super(model, FORM_ID, Conversation.PROPERTY_REQUEST_HEADERS,
				Conversation.PROPERTY_REQUEST_CONTENT, editable);
	}

	@Override
	protected JComponent getParsedHeaderComponent() {
		return new JLabel("Parsed requests not yet implemented");
	}

	
}
