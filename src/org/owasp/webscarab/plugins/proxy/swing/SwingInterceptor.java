/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.io.IOException;

import org.owasp.webscarab.Annotation;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.plugins.proxy.ProxyInterceptor;
import org.owasp.webscarab.ui.forms.RawRequestForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.ui.forms.support.ConversationFormSupport;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;

/**
 * @author rdawes
 *
 */
public class SwingInterceptor implements ProxyInterceptor {

	public SwingInterceptor() {
	}
	
	public void editRequest(Conversation conversation, Annotation annotation) throws IOException {
		ValidatingFormModel model = ConversationFormSupport.createRequestFormModel(conversation);
		final RequestForm requestForm = new RequestForm(model);
		final DialogPage page = new FormBackedDialogPage(requestForm);
		TitledPageApplicationDialog dialog = new TitledPageApplicationDialog(page, null) {
			protected void onAboutToShow() {
				setEnabled(page.isPageComplete());
			}
			
			protected boolean onFinish() {
				System.out.println("Commit!");
				requestForm.commit();
				return true;
			}
		};
		dialog.getDialog().setSize(800, 600);
		dialog.showDialog();
	}

	public void editResponse(Conversation conversation, Annotation annotation) throws IOException {
		ValidatingFormModel requestModel = ConversationFormSupport.createRequestFormModel(conversation);
		final RequestForm requestForm = new RequestForm(requestModel);
		requestForm.setEnabled(false);
		ValidatingFormModel responseModel = ConversationFormSupport.createResponseFormModel(conversation);
		final ResponseForm responseForm = new ResponseForm(responseModel);
		responseForm.setEnabled(true);
	}

}
