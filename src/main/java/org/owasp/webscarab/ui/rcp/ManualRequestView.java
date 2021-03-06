/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.services.ConversationGenerator;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.services.HttpService;
import org.owasp.webscarab.ui.rcp.forms.AnnotationForm;
import org.owasp.webscarab.ui.rcp.forms.RequestForm;
import org.owasp.webscarab.ui.rcp.forms.ResponseForm;
import org.owasp.webscarab.ui.rcp.forms.support.ConversationFormSupport;
import org.owasp.webscarab.util.TransformRequestCommand;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.support.DialogPageUtils;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;

/**
 * @author rdawes
 *
 */
public class ManualRequestView extends AbstractView {

	private FormModel conversationFormModel;
	private FormModel annotationFormModel;

    private EventService eventService;

    private HttpService httpService;

    private Session session;

    private Fetcher activeFetcher;

    private Listener listener;

    private ConversationService conversationService;

    private RequestEditListener editListener = new RequestEditListener();

    private FetchCommand fetchCommand;

    private RevertCommand revertCommand;
    
    private String[] requestFields = new String[] {
            Conversation.PROPERTY_REQUEST_METHOD,
            Conversation.PROPERTY_REQUEST_URI,
            Conversation.PROPERTY_REQUEST_VERSION,
            Conversation.PROPERTY_REQUEST_HEADERS,
            Conversation.PROPERTY_REQUEST_CONTENT,
            Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT,
    };

    private String[] responseFields = new String[] {
            Conversation.PROPERTY_RESPONSE_VERSION,
            Conversation.PROPERTY_RESPONSE_STATUS,
            Conversation.PROPERTY_RESPONSE_MESSAGE,
            Conversation.PROPERTY_RESPONSE_HEADERS,
            Conversation.PROPERTY_RESPONSE_CONTENT,
            Conversation.PROPERTY_RESPONSE_FOOTERS,
    };

	public ManualRequestView() {
		conversationFormModel = ConversationFormSupport.createFormModel(new Conversation(), false, true, false);
		annotationFormModel = FormModelHelper.createUnbufferedFormModel(new Annotation());
        addEditListener();
        listener = new Listener();
        fetchCommand = new FetchCommand();
        revertCommand = new RevertCommand();
	}

    private void addEditListener() {
        for (int i=0, len=requestFields.length; i<len; i++) {
            ValueModel vm = conversationFormModel.getValueModel(requestFields[i]);
            vm.addValueChangeListener(editListener);
        }
    }

    private void removeEditListener() {
        for (int i=0, len=requestFields.length; i<len; i++) {
            ValueModel vm = conversationFormModel.getValueModel(requestFields[i]);
            vm.removeValueChangeListener(editListener);
        }
    }

	/* (non-Javadoc)
	 * @see org.springframework.richclient.dialog.AbstractDialogPage#createControl()
	 */
	@Override
	protected JComponent createControl() {
        Form requestForm = new RequestForm(conversationFormModel);
        Form responseForm = new ResponseForm(conversationFormModel);
        Form annotationForm = new AnnotationForm(annotationFormModel);
        DialogPage page = new ManualRequestDialogPage(requestForm, responseForm, annotationForm);
        return DialogPageUtils.createStandardView(page, fetchCommand, revertCommand);
	}

    public void displayConversation(Conversation conversation) {
        Conversation request;
        if (conversation != null) {
            request = conversation.clone();
        } else {
            request = new Conversation();
        }
        removeEditListener();
        conversationFormModel.setFormObject(request);
        addEditListener();
    }

    private boolean isActiveFetcher(Fetcher fetcher) {
        return activeFetcher == fetcher;
    }

    private void fetchConversation(Conversation request) {
        request.setDate(new Date());
        activeFetcher = new Fetcher(request);
        httpService.fetchResponses(activeFetcher, 1, false);
    }

    private void error(Exception e) {
        e.printStackTrace();
    }

    private void success(Conversation conversation) {
        getConversationService().addConversation(session, conversation);
        Annotation a = (Annotation) annotationFormModel.getFormObject();
        Annotation annotation = new Annotation();
        annotation.setAnnotation(a.getAnnotation());
        annotation.setId(conversation.getId());
        getConversationService().updateAnnotation(annotation);
        displayConversation(conversation);
    }

    public Conversation getConversation() {
        Conversation conversation = null;
        conversation = (Conversation) conversationFormModel.getFormObject();
        return conversation;
    }
    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        if (this.eventService != null) {
            this.eventService.unsubscribe(ManualRequestCopyEvent.class, listener);
            this.eventService.unsubscribe(SessionEvent.class, listener);
        }
        this.eventService = eventService;
        if (this.eventService != null) {
            this.eventService.subscribe(ManualRequestCopyEvent.class, listener);
            this.eventService.subscribe(SessionEvent.class, listener);
        }
    }

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * @return Returns the conversationService.
     */
    private ConversationService getConversationService() {
        if (conversationService == null)
            conversationService = (ConversationService) getApplicationContext()
                    .getBean("conversationService");
        return conversationService;
    }

    private class Listener extends SwingEventSubscriber {

        /* (non-Javadoc)
         * @see org.owasp.webscarab.ui.SwingEventSubscriber#handleEventOnEDT(org.bushe.swing.event.EventServiceEvent)
         */
        @Override
        protected void handleEventOnEDT(EventServiceEvent evt) {
            if (evt instanceof ManualRequestCopyEvent) {
                handleEvent((ManualRequestCopyEvent) evt);
            }
            if (evt instanceof SessionEvent) {
                handleEvent((SessionEvent) evt);
            }
        }

        private void handleEvent(ManualRequestCopyEvent mrce) {
            Object source = mrce.getSource();
            if (!(source instanceof PageComponent)) return;
            PageComponent pc = (PageComponent) source;
            if (! pc.getContext().getPage().equals(getContext().getPage())) return;
            displayConversation(mrce.getConversation());
        }

        private void handleEvent(SessionEvent  evt) {
            session = evt.getSession();
            displayConversation(null);
        }
    }

    private class Fetcher implements ConversationGenerator {

        private Conversation request;

        private boolean executed = false;

        public Fetcher(Conversation request) {
            this.request = request;
        }

        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#errorFetchingResponse(org.owasp.webscarab.domain.Conversation, java.lang.Exception)
         */
        public void errorFetchingResponse(final Conversation request, final Exception e) {
            if (!isActiveFetcher(this)) return;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    error(e);
                }
            });
        }

        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#getNextRequest()
         */
        public synchronized Conversation getNextRequest() {
            if (!isActiveFetcher(this)) return null;
            if (executed) return null;
            executed = true;
            return request;
        }

        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#responseReceived(org.owasp.webscarab.domain.Conversation)
         */
        public void responseReceived(final Conversation conversation) {
            if (!isActiveFetcher(this)) return;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    success(conversation);
                }
            });
        }

    }

    private class RequestEditListener implements PropertyChangeListener {

        /*
         * Ensures that any response fields are cleared as soon as the user starts editing the request fields
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {
            for (int i=0, len=responseFields.length; i<len; i++) {
                ValueModel vm = conversationFormModel.getValueModel(responseFields[i]);
                if (vm.getValue() != null)
                    vm.setValue(null);
            }
        }

    }

    private class FetchCommand extends ActionCommand {
        public FetchCommand() {
            super("fetchCommand");
        }
        protected void doExecuteCommand() {
            Conversation conversation = getConversation();
            fetchConversation(conversation);
        }
    }

    private class RevertCommand extends ActionCommand {
        public RevertCommand() {
            super("revertCommand");
        }
        protected void doExecuteCommand() {
            conversationFormModel.revert();
            annotationFormModel.revert();
        }
    }

    /**
     * @param httpService the httpService to set
     */
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    private class ManualRequestDialogPage extends FormBackedDialogPage {
        private Form requestForm;

        private Form responseForm;

        private Form annotationForm;

        public ManualRequestDialogPage(Form requestForm, Form responseForm,
                Form annotationForm) {
            super("manualRequestForm", requestForm);
            this.requestForm = requestForm;
            this.responseForm = responseForm;
            this.annotationForm = annotationForm;
        }

        @Override
        protected JComponent createControl() {
            JPanel panel = getComponentFactory()
                    .createPanel(new BorderLayout());
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.5);
            JPanel requestPanel = new JPanel(new BorderLayout());
            requestPanel.add(requestForm.getControl(), BorderLayout.CENTER);
            CommandGroup  transformCommands = getWindowCommandManager().createCommandGroup("transformRequest", 
            		new Object[] {
            		TransformRequestCommand.createGetToPost(conversationFormModel),
            		TransformRequestCommand.createPostToMultipartPost(conversationFormModel),
            		TransformRequestCommand.createPostToGet(conversationFormModel),
            });
            requestPanel.add(transformCommands.createButtonBar(), BorderLayout.SOUTH);
            splitPane.setLeftComponent(requestPanel);
            splitPane.setRightComponent(responseForm.getControl());
            panel.add(splitPane, BorderLayout.CENTER);
            panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
            initPageValidationReporter();
            responseForm.getFormModel().validate();
            return panel;
        }

    }

}
