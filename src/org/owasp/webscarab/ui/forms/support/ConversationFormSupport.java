/**
 * 
 */
package org.owasp.webscarab.ui.forms.support;

import org.owasp.webscarab.Conversation;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class ConversationFormSupport {

	public static ValidatingFormModel createRequestFormModel(Conversation c) {
		ValidatingFormModel model = FormModelHelper.createFormModel(c, true);
//		model.setValidator(ConversationValidator.REQUEST_VALIDATOR);
//		model.setValidating(true);
		return model;
	}
	
	public static ValidatingFormModel createResponseFormModel(Conversation c) {
		ValidatingFormModel model = FormModelHelper.createFormModel(c, true);
		return model;
	}
	
}
