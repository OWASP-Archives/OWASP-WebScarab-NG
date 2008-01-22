/**
 * 
 */
package org.owasp.webscarab.plugins.webservices.swing;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.plugins.webservices.Wsdl;
import org.owasp.webscarab.services.ConversationGenerator;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.services.HttpService;
import org.owasp.webscarab.ui.rcp.ManualRequestCopyEvent;
import org.owasp.webscarab.ui.rcp.SwingEventSubscriber;
import org.owasp.webscarab.ui.rcp.forms.AnnotationForm;
import org.owasp.webscarab.ui.rcp.forms.ContentTabbedPane;
import org.owasp.webscarab.ui.rcp.forms.support.ConversationFormSupport;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.GuardedActionCommandExecutor;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.dialog.AbstractDialogPage;
import org.springframework.richclient.dialog.DialogPage;
import org.springframework.richclient.dialog.support.DialogPageUtils;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.form.builder.GridBagLayoutFormBuilder;
import org.springframework.richclient.layout.LabelOrientation;
import org.springframework.richclient.list.DynamicComboBoxListModel;
import org.xml.sax.SAXException;

/**
 * @author rdawes
 *
 */
public class WebServicesView extends AbstractView {

    private FormModel conversationFormModel;
    private FormModel annotationFormModel;

    private EventService eventService;

    private HttpService httpService;

    private Session session;

    private ConversationGenerator activeFetcher;

    private Listener listener;

    private ConversationService conversationService;

    private FetchCommand fetchCommand;

    private RevertCommand revertCommand;

    private Wsdl wsdl;
    
    private WsdlParams wsdlParams;
    
    private Definition definition;
    
    private GuardedActionCommandExecutor manualRequestExecutor = new ManualRequestExecutor();

    private WsdlUriForm wsdlUriForm;
    
    public WebServicesView() {
        conversationFormModel = ConversationFormSupport.createFormModel(new Conversation(), false, true, false);
        annotationFormModel = FormModelHelper.createUnbufferedFormModel(new Annotation());
        listener = new Listener();
        fetchCommand = new FetchCommand();
        revertCommand = new RevertCommand();
        wsdlParams = new WsdlParams();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.springframework.richclient.application.support.AbstractView#registerLocalCommandExecutors(org.springframework.richclient.application.PageComponentContext)
     */
    @Override
    protected void registerLocalCommandExecutors(PageComponentContext context) {
        context.register("manualRequestCommand", manualRequestExecutor);
    }

    /* (non-Javadoc)
     * @see org.springframework.richclient.dialog.AbstractDialogPage#createControl()
     */
    @Override
    protected JComponent createControl() {
        wsdlUriForm = new WsdlUriForm(FormModelHelper.createFormModel(wsdlParams, false));
        ContentTabbedPane requestPane = new ContentTabbedPane(conversationFormModel, Conversation.PROPERTY_REQUEST_HEADERS,
                Conversation.PROPERTY_REQUEST_PROCESSED_CONTENT);
        ContentTabbedPane responsePane = new ContentTabbedPane(conversationFormModel, Conversation.PROPERTY_RESPONSE_HEADERS,
                Conversation.PROPERTY_RESPONSE_PROCESSED_CONTENT);
        Form annotationForm = new AnnotationForm(annotationFormModel);
        DialogPage page = new WebServicesDialogPage(wsdlUriForm, requestPane, responsePane, annotationForm);
        return DialogPageUtils.createStandardView(page, fetchCommand, revertCommand);
    }

    public void displayConversation(Conversation conversation) {
        Conversation request;
        if (conversation != null) {
            request = conversation.clone();
        } else {
            request = new Conversation();
        }
        conversationFormModel.setFormObject(request);
    }

    private boolean isActiveFetcher(ConversationGenerator fetcher) {
        return activeFetcher == fetcher;
    }

    private void fetchConversation(Conversation request) {
        request.setDate(new Date());
        activeFetcher = new RequestFetcher(request);
        httpService.fetchResponses(activeFetcher, 1, true);
    }

    private void error(Exception e) {
        e.printStackTrace();
    }

    public Conversation getConversation() {
        Conversation conversation = null;
        conversation = (Conversation) conversationFormModel.getFormObject();
        return conversation;
    }

    private void fetchWsdl() {
        definition = null;
        wsdlUriForm.setOperations(null);
        try {
            final URI uri = new URI(wsdlParams.getWsdlUri());
            activeFetcher = new WsdlFetcher(uri);
            httpService.fetchResponses(activeFetcher, 1, true);
        } catch (URISyntaxException use) {
            error(use);
        }
    }
    
    private void constructConversation() {
        if (definition == null || wsdlParams == null) {
            displayConversation(new Conversation());
            return;
        }
        try {
            Conversation c = wsdl.constructRequest(definition, wsdlParams.getWsdlOperation());
            displayConversation(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        if (this.eventService != null) {
            this.eventService.unsubscribe(SessionEvent.class, listener);
        }
        this.eventService = eventService;
        if (this.eventService != null) {
            this.eventService.subscribe(SessionEvent.class, listener);
        }
    }

    /**
     * @return the eventService
     */
    public EventService getEventService() {
        return this.eventService;
    }

    /**
     * @param httpService the httpService to set
     */
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
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

    public void setWsdl(Wsdl wsdl) {
        this.wsdl = wsdl;
    }

    private static class WebServicesDialogPage extends AbstractDialogPage {
        
        private WsdlUriForm wsdlUriForm;
        
        private ContentTabbedPane requestPane, responsePane;

        private Form annotationForm;

        public WebServicesDialogPage(WsdlUriForm wsdlUriForm, ContentTabbedPane requestPane, ContentTabbedPane responsePane,
                Form annotationForm) {
            super("webservicesForm");
            this.wsdlUriForm = wsdlUriForm;
            this.requestPane = requestPane;
            this.responsePane = responsePane;
            this.annotationForm = annotationForm;
        }

        @Override
        protected JComponent createControl() {
            JPanel panel = getComponentFactory()
                    .createPanel(new BorderLayout());
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.5);
            splitPane.setLeftComponent(requestPane);
            splitPane.setRightComponent(responsePane);
            panel.add(wsdlUriForm.getControl(), BorderLayout.NORTH);
            panel.add(splitPane, BorderLayout.CENTER);
            panel.add(annotationForm.getControl(), BorderLayout.SOUTH);
            return panel;
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

    private class Listener extends SwingEventSubscriber {

        /* (non-Javadoc)
         * @see org.owasp.webscarab.ui.SwingEventSubscriber#handleEventOnEDT(org.bushe.swing.event.EventServiceEvent)
         */
        @Override
        protected void handleEventOnEDT(EventServiceEvent evt) {
            if (evt instanceof SessionEvent) {
                handleEvent((SessionEvent) evt);
            }
        }

        private void handleEvent(SessionEvent  evt) {
            session = evt.getSession();
            displayConversation(null);
        }
    }

    private class WsdlFetcher implements ConversationGenerator {
        private Conversation request;
        public WsdlFetcher(URI uri) {
            this.request = new Conversation();
            request.setRequestMethod("GET");
            request.setRequestUri(uri);
            request.setRequestVersion("HTTP/1.0");
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
            Conversation r = request;
            request = null;
            return r;
        }

        /* (non-Javadoc)
         * @see org.owasp.webscarab.services.ConversationGenerator#responseReceived(org.owasp.webscarab.domain.Conversation)
         */
        public void responseReceived(final Conversation conversation) {
            if (!isActiveFetcher(this)) return;
            getConversationService().addConversation(session, conversation);
            InputStream wsdlStream;
            String contentType = conversation.getResponseHeader("Content-Type");
            if (contentType != null && contentType.startsWith("text/xml")) {
                wsdlStream = new ByteArrayInputStream(conversation.getResponseContent());
            } else {
                error(new IOException("Invalid Content-Type: " + contentType));
                return;
            }
            try {
                definition = wsdl.getWSDL(conversation.getRequestUri(), wsdlStream);
                final String[] operations = wsdl.getOperations(definition);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        wsdlUriForm.setOperations(operations);
                    }
                });
            } catch (IOException ioe) {
                error(ioe);
            } catch (SAXException saxe) {
                error(saxe);
            } catch (WSDLException we) {
                error(we);
            }
        }
    }
    
    private class RequestFetcher implements ConversationGenerator {

        private Conversation request;

        private boolean executed = false;

        public RequestFetcher(Conversation request) {
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
                    getConversationService().addConversation(session, conversation);
                    Annotation a = (Annotation) annotationFormModel.getFormObject();
                    Annotation annotation = new Annotation();
                    annotation.setAnnotation(a.getAnnotation());
                    annotation.setId(conversation.getId());
                    getConversationService().updateAnnotation(annotation);
                    displayConversation(conversation);
                }
            });
        }

    }

    private class ManualRequestExecutor extends AbstractActionCommandExecutor {
        public ManualRequestExecutor() {
            setEnabled(true);
        }
        public void execute() {
            getContext().getPage().showView("manualRequestView");
            ManualRequestCopyEvent mrce = new ManualRequestCopyEvent(
                    WebServicesView.this, getConversation());
            getEventService().publish(mrce);
        }
    }

    private class WsdlUriForm extends AbstractForm {
        private ValueModel operationsValueModel;
        public WsdlUriForm(FormModel model) {
            super(model, "wsdluriForm");
        }

        @SuppressWarnings("unchecked")
        public void setOperations(String[] operations) {
            List<String> list;
            if (operations != null && operations.length > 0) {
                list = Arrays.asList(operations);
            } else {
                list = Collections.EMPTY_LIST;
                getFormModel().getValueModel("wsdlOperation").setValue(null);
            }
            operationsValueModel.setValue(list);
        }
        
        @Override
        protected JComponent createFormControl() {
            operationsValueModel = new ValueHolder();
            operationsValueModel.setValue(Collections.EMPTY_LIST);
            FetchCommand fetchCommand = new FetchCommand();
            ConstructConversationCommand ccCommand = new ConstructConversationCommand();
            getCommandConfigurer().configure(fetchCommand);
            getCommandConfigurer().configure(ccCommand);
            JComboBox comboBox = getComponentFactory().createComboBox();
            comboBox.setModel(new DynamicComboBoxListModel(getFormModel().getValueModel("wsdlOperation"), operationsValueModel));
            comboBox.addItemListener(ccCommand);
//            TableFormBuilder builder = new TableFormBuilder(getBindingFactory());
//            builder.add("wsdlUri");
//            builder.getLayoutBuilder().unrelatedGapCol().cell(fetchCommand.createButton(), "colSpan=1");
//            builder.row();
//            builder.add("wsdlOperation", comboBox);
//            builder.getLayoutBuilder().unrelatedGapCol().cell(ccCommand.createButton(), "colSpan=1");
//            builder.row();
//            return builder.getForm();
            GridBagLayoutFormBuilder builder = new GridBagLayoutFormBuilder(getBindingFactory());
            builder.appendLabeledField("wsdlUri");
            builder.getBuilder().append(fetchCommand.createButton());
            builder.nextLine();
            builder.appendLabeledField("wsdlOperation", comboBox, LabelOrientation.LEFT);
            builder.getBuilder().append(ccCommand.createButton());
            return builder.getPanel();
        }
        
        private class FetchCommand extends ActionCommand {
            public FetchCommand() {
                super("fetchCommand");
            }
            protected void doExecuteCommand() {
                fetchWsdl();
            }
        }
        
        private class ConstructConversationCommand extends ActionCommand implements ItemListener {
            public ConstructConversationCommand() {
                super("constructConversationCommand");
            }
            protected void doExecuteCommand() {
                constructConversation();
            }
            public void itemStateChanged(ItemEvent e) {
                doExecuteCommand();
            }
        }
    }
    
    private class WsdlParams {
        private String WsdlUri = "http://localhost./WebGoat/services/SoapRequest?WSDL";
        private String wsdlOperation;
        
        public String getWsdlUri() {
            return this.WsdlUri;
        }
        public void setWsdlUri(String wsdlUri) {
            this.WsdlUri = wsdlUri;
        }
        public String getWsdlOperation() {
            return this.wsdlOperation;
        }
        public void setWsdlOperation(String operation) {
            this.wsdlOperation = operation;
        }
    }
}
