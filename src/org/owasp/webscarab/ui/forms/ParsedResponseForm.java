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
public class ParsedResponseForm extends AbstractParsedContentForm {

	private static final String FORM_ID = "parsedResponseForm";
	
	public ParsedResponseForm(FormModel model) {
		super(model, FORM_ID, Conversation.PROPERTY_RESPONSE_HEADERS,
				Conversation.PROPERTY_RESPONSE_CONTENT, true);
	}

	@Override
	protected JComponent getParsedHeaderComponent() {
		return new JLabel("No parsed responses implemented yet!");
	}

}
