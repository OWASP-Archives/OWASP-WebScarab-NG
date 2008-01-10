/**
 * 
 */
package org.owasp.webscarab.ui.rcp.forms.support;

import org.owasp.webscarab.domain.Conversation;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class ConversationFormSupport {

	public static FormModel createBufferedConversationFormModel(Conversation c, boolean requestEditable, boolean responseEditable) {
		return createFormModel(c, true, requestEditable, responseEditable);
	}
	
	public static FormModel createReadOnlyFormModel(Conversation c) {
		return createFormModel(c, false, false, false);
	}
	
	public static FormModel createFormModel(Conversation c, boolean buffered, boolean requestEditable, boolean responseEditable) {
		FormModel model = FormModelHelper.createFormModel(c, buffered);
		setRequestEditable(model, requestEditable);
		setResponseEditable(model, responseEditable);
		return model;
	}
	
	private static void setRequestEditable(FormModel model, boolean editable) {
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_METHOD).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_URI).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_VERSION).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_HEADERS).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_CONTENT).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT).setReadOnly(!editable);
	}
	
	private static void setResponseEditable(FormModel model, boolean editable) {
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_VERSION).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_STATUS).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_MESSAGE).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_HEADERS).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_CONTENT).setReadOnly(!editable);
		model.getFieldMetadata(Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT).setReadOnly(!editable);
	}
}
