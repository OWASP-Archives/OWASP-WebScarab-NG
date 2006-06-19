/**
 * 
 */
package org.owasp.webscarab.plugins.proxy.swing;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.plugins.proxy.ProxyInterceptor;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
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

	private boolean interceptRequests = false;

	private boolean interceptResponses = false;

	private String interceptRequestRegex = ".*(\\.(gif|jpg|png|css|js|ico|swf|axd.*)|refresher.asp)$";

	public SwingInterceptor() {
	}

	public synchronized void editRequest(Conversation conversation,
			Annotation annotation) throws IOException {
		String uri = conversation.getRequestUri().toASCIIString();

		if (!interceptRequests || uri.matches(interceptRequestRegex))
			return;

		final ValidatingFormModel requestModel = FormModelHelper
				.createFormModel(conversation, true);
		final ValidatingFormModel annotationModel = FormModelHelper
				.createFormModel(annotation, true);
		final RequestForm requestForm = new RequestForm(requestModel, true);
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
		if (!interceptResponses) return;
		String contentType = conversation.getResponseHeader("Content-Type");
		if (contentType == null || !contentType.startsWith("text"))
			return;
		final ValidatingFormModel model = FormModelHelper.createFormModel(
				conversation, true);
		final RequestForm requestForm = new RequestForm(model, false);
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
			return panel;
		}

	}

	public String getInterceptRequestRegex() {
		return this.interceptRequestRegex;
	}

	public void setInterceptRequestRegex(String interceptRequestRegex) {
		this.interceptRequestRegex = interceptRequestRegex;
	}

	public boolean isInterceptRequests() {
		return this.interceptRequests;
	}

	public void setInterceptRequests(boolean interceptRequests) {
		this.interceptRequests = interceptRequests;
	}

	public boolean isInterceptResponses() {
		return this.interceptResponses;
	}

	public void setInterceptResponses(boolean interceptResponses) {
		this.interceptResponses = interceptResponses;
	}

}
