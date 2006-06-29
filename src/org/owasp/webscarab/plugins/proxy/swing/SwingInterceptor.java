/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.plugins.proxy.ProxyInterceptor;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.ui.forms.support.ConversationFormSupport;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 * 
 */
public class SwingInterceptor implements ProxyInterceptor {

	private List<String> interceptRequestMethods = null;
	
	private List<Pattern> interceptResponseTypes = null;
	
	private String skipRequestRegex = ".*(\\.(gif|jpg|png|css|js|ico|swf|axd.*)|refresher.asp)$";

	private boolean interceptResponses = false;
	
	private boolean interceptAllResponses = false;
	
	public SwingInterceptor() {
	}

	public synchronized void editRequest(Conversation conversation,
			Annotation annotation) throws IOException {
		String method = conversation.getRequestMethod();
		String uri = conversation.getRequestUri().toASCIIString();
		
		if (interceptRequestMethods == null || !interceptRequestMethods.contains(method))
			return;
		if (uri.matches(skipRequestRegex))
			return;

		final ValidatingFormModel requestModel = ConversationFormSupport
				.createBufferedConversationFormModel(conversation, true, false);
		final ValidatingFormModel annotationModel = FormModelHelper
				.createFormModel(annotation, true);
		final RequestForm requestForm = new RequestForm(requestModel);
		final AnnotationForm annotationForm = new AnnotationForm(
				annotationModel);
		final DialogPage page = new InterceptRequestDialogPage(requestForm,
				annotationForm);
		final ActionCommand okCommand = new ActionCommand("okCommand") {
			protected void doExecuteCommand() {
				if (requestModel.isDirty())
					requestModel.commit();
				if (annotationModel.isDirty())
					annotationModel.commit();
			}
		};
		final ActionCommand cancelCommand = new ActionCommand("cancelCommand") {
			protected void doExecuteCommand() {
			}
		};
		DialogPageFrame frame = new DialogPageFrame(page, okCommand,
				cancelCommand);
		frame.showAsDialog();
	}

	public synchronized void editResponse(Conversation conversation,
			Annotation annotation) throws IOException {
		if (!interceptResponses)
			return;
		String contentType = conversation.getResponseHeader("Content-Type");
		boolean intercept = interceptAllResponses;
		if (! intercept && contentType != null && interceptResponseTypes != null) {
			Iterator<Pattern> it = interceptResponseTypes.iterator();
			while (it.hasNext()) {
				Pattern pattern = it.next();
				if (pattern.matcher(contentType).matches())
					intercept = true;
			}
		}
		if (!intercept)
			return;
		final ValidatingFormModel model = FormModelHelper.createFormModel(
				conversation, true);
		final RequestForm requestForm = new RequestForm(model);
		final ResponseForm responseForm = new ResponseForm(model);
		final ValidatingFormModel annotationModel = FormModelHelper
				.createFormModel(annotation, true);
		final AnnotationForm annotationForm = new AnnotationForm(
				annotationModel);
		final DialogPage page = new InterceptResponseDialogPage(requestForm,
				responseForm, annotationForm);
		final ActionCommand okCommand = new ActionCommand("okCommand") {
			protected void doExecuteCommand() {
				if (model.isDirty())
					model.commit();
				if (annotationModel.isDirty())
					annotationModel.commit();
			}
		};
		final ActionCommand cancelCommand = new ActionCommand("cancelCommand") {
			protected void doExecuteCommand() {
			}
		};
		DialogPageFrame frame = new DialogPageFrame(page, okCommand,
				cancelCommand);
		frame.showAsDialog();
	}

	private class InterceptRequestDialogPage extends FormBackedDialogPage {
		private Form requestForm;

		private Form annotationForm;

		public InterceptRequestDialogPage(Form requestForm, Form annotationForm) {
			super(requestForm, true);
			this.requestForm = requestForm;
			this.annotationForm = annotationForm;
		}

		@Override
		protected JComponent createControl() {
			JPanel panel = getComponentFactory()
					.createPanel(new BorderLayout());
			panel.add(requestForm.getControl(), BorderLayout.CENTER);
			panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
			initPageValidationReporter();
			requestForm.getFormModel().validate();
			return panel;
		}

	}

	private class InterceptResponseDialogPage extends FormBackedDialogPage {
		private Form requestForm;

		private Form responseForm;

		private Form annotationForm;

		public InterceptResponseDialogPage(Form requestForm, Form responseForm,
				Form annotationForm) {
			super(responseForm, true);
			this.requestForm = requestForm;
			this.responseForm = responseForm;
			this.annotationForm = annotationForm;
		}

		@Override
		protected JComponent createControl() {
			JPanel panel = getComponentFactory()
					.createPanel(new BorderLayout());
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setResizeWeight(0.3);
			splitPane.setLeftComponent(requestForm.getControl());
			splitPane.setRightComponent(responseForm.getControl());
			panel.add(splitPane, BorderLayout.CENTER);
			panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
			initPageValidationReporter();
			responseForm.getFormModel().validate();
			return panel;
		}

	}

	public String getSkipRequestRegex() {
		return this.skipRequestRegex;
	}

	public void setSkipRequestRegex(String interceptRequestRegex) {
		this.skipRequestRegex = interceptRequestRegex;
	}

	public boolean isInterceptAllResponses() {
		return this.interceptAllResponses;
	}

	public void setInterceptAllResponses(boolean interceptAllResponses) {
		this.interceptAllResponses = interceptAllResponses;
	}

	public List<String> getInterceptRequestMethods() {
		return this.interceptRequestMethods;
	}

	public void setInterceptRequestMethods(List<String> interceptRequestMethods) {
		this.interceptRequestMethods = interceptRequestMethods;
	}

	public List<Pattern> getInterceptResponseTypes() {
		return this.interceptResponseTypes;
	}

	public void setInterceptResponseTypes(List<Pattern> interceptResponseTypes) {
		this.interceptResponseTypes = interceptResponseTypes;
	}

	public boolean isInterceptResponses() {
		return this.interceptResponses;
	}

	public void setInterceptResponses(boolean interceptResponses) {
		this.interceptResponses = interceptResponses;
	}

}
