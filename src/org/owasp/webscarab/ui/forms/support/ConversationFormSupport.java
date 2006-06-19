/**
 * 
 */
package org.owasp.webscarab.ui.forms.support;

import org.owasp.webscarab.domain.Conversation;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class ConversationFormSupport {

	public static ValidatingFormModel createBufferedConversationFormModel(Conversation c, boolean requestEditable, boolean responseEditable) {
		ValidatingFormModel model = FormModelHelper.createFormModel(c, true);
		setRequestEditable(model, requestEditable);
		setResponseEditable(model, responseEditable);
		return model;
	}
	
	public static ValidatingFormModel createReadOnlyFormModel(Conversation c) {
		ValidatingFormModel model = FormModelHelper.createFormModel(c, false);
		setRequestEditable(model, false);
		setResponseEditable(model, false);
		return model;
	}
	
	private static void setRequestEditable(FormModel model, boolean editable) {
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_METHOD).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_URI).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_VERSION).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_HEADERS).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_CONTENT).setReadOnly(!editable);
	}
	
	private static void setResponseEditable(FormModel model, boolean editable) {
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_VERSION).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_STATUS).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_MESSAGE).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_HEADERS).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_CONTENT).setReadOnly(!editable);
	}
}
