/**
 *
 */
package org.owasp.webscarab.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.ui.forms.AnnotationForm;
import org.owasp.webscarab.ui.forms.RequestForm;
import org.owasp.webscarab.ui.forms.ResponseForm;
import org.owasp.webscarab.ui.forms.support.ConversationFormSupport;
import org.springframework.binding.form.CommitListener;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class ConversationView extends AbstractView {

	private Form requestForm;

	private Form responseForm;

	private Form annotationForm;

    private EventService eventService;

	private ConversationService conversationService;

	private FormModel conversationModel;

	private FormModel annotationModel;

    private Listener listener = new Listener();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.richclient.application.support.AbstractView#createControl()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected JComponent createControl() {
		Conversation conversation = new Conversation();
		Annotation annotation = new Annotation();

		conversationModel = ConversationFormSupport
				.createReadOnlyFormModel(conversation);
		annotationModel = FormModelHelper.createFormModel(annotation, false);
		requestForm = new RequestForm(conversationModel);
		responseForm = new ResponseForm(conversationModel);
		annotationForm = new AnnotationForm(annotationModel);

		annotationModel.addCommitListener(new AnnotationListener());

		JSplitPane conversationSplitPane = new JSplitPane();
		conversationSplitPane.setOneTouchExpandable(true);
		conversationSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		conversationSplitPane.setResizeWeight(0.5);
		conversationSplitPane.setTopComponent(requestForm.getControl());
		conversationSplitPane.setBottomComponent(responseForm.getControl());
        JPanel panel = getComponentFactory().createPanel(new BorderLayout());
		panel.add(conversationSplitPane, BorderLayout.CENTER);
		panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
		return panel;
	}


	private void updateSelection(Conversation conversation) {
		if (annotationModel.isDirty())
			annotationModel.commit();
		if (conversation != null) {
			conversationModel.setFormObject(getConversationService()
					.getConversation(conversation.getId()));
			Annotation annotation = getConversationService().getAnnotation(
					conversation.getId());
			if (annotation == null) {
				annotation = new Annotation();
				annotation.setId(conversation.getId());
			}
			annotationModel.setFormObject(annotation);
		} else {
			conversationModel.setFormObject(new Conversation());
			annotationModel.setFormObject(new Annotation());
		}
	}

	/**
	 * @return Returns the conversationService.
	 */
	public ConversationService getConversationService() {
		if (conversationService == null)
			conversationService = (ConversationService) getApplicationContext()
					.getBean("conversationService");
		return conversationService;
	}

	private class AnnotationListener implements CommitListener {

		public void postCommit(FormModel formModel) {
			getConversationService().updateAnnotation((Annotation)formModel.getFormObject());
		}
		public void preCommit(FormModel formModel) {
		}
	}

    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
        if (eventService != null) {
            eventService.subscribe(ConversationSelectionEvent.class, listener);
        }
    }

    /**
     * @return the eventService
     */
    public EventService getEventService() {
        return this.eventService;
    }

    private class Listener extends SwingEventSubscriber {

        /* (non-Javadoc)
         * @see org.owasp.webscarab.ui.SwingEventSubscriber#handleEventOnEDT(org.bushe.swing.event.EventServiceEvent)
         */
        @Override
        protected void handleEventOnEDT(EventServiceEvent evt) {
            if (!(evt instanceof ConversationSelectionEvent))
                return;
            ConversationSelectionEvent cse = (ConversationSelectionEvent) evt;
            Object source = cse.getSource();
            if (!(source instanceof PageComponent))
                return;
            PageComponent pc = (PageComponent) source;
            if (!pc.getContext().getPage().equals(getContext().getPage()))
                return;
            Conversation[] selection = cse.getSelection();
            if (selection == null || selection.length != 1) {
                updateSelection(null);
            } else {
                updateSelection(selection[0]);
            }
        }

    }

}
