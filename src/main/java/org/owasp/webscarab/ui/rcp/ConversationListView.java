/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.URI;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.services.ConversationService;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.GuardedActionCommandExecutor;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.list.ListSelectionValueModelAdapter;
import org.springframework.richclient.list.ListSingleSelectionGuard;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

/**
 * @author rdawes
 *
 */
public class ConversationListView extends AbstractView {

    private EventList<Conversation> conversationList;

    private ConversationService conversationService;

    private EventService eventService;

    private ConversationTableFactory conversationTableFactory;

    private EventSelectionModel<Conversation> conversationSelectionModel;

    private GuardedActionCommandExecutor manualRequestExecutor = new ManualRequestExecutor();

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.richclient.application.support.AbstractView#registerLocalCommandExecutors(org.springframework.richclient.application.PageComponentContext)
     */
    @Override
    protected void registerLocalCommandExecutors(PageComponentContext context) {
        context.register("manualRequestCommand", manualRequestExecutor);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.richclient.application.support.AbstractView#createControl()
     */
    @Override
    protected JComponent createControl() {
        JTextField filterField = getComponentFactory().createTextField();
        UriMatcher uriMatcher = new UriMatcher();

        EventList<Conversation> conversationList = getConversationList();
        FilterList<Conversation> uriFilterList = new FilterList<Conversation>(
                conversationList, uriMatcher);
        TextFilterator<Conversation> filterator = new ConversationFilter();
        MatcherEditor<Conversation> matcher = new TextComponentMatcherEditor<Conversation>(
                filterField, filterator);
        FilterList<Conversation> filterList = new FilterList<Conversation>(
                uriFilterList, matcher);
        SortedList<Conversation> sortedList = new SortedList<Conversation>(
                filterList);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(getComponentFactory().createLabelFor("filter",
                filterField));
        filterPanel.add(filterField);

        JTable table = getConversationTableFactory().getConversationTable(
                sortedList);
        conversationSelectionModel = new EventSelectionModel<Conversation>(
                sortedList);
        table.setSelectionModel(conversationSelectionModel);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting())
                            return;
                        EventList<Conversation> selected = conversationSelectionModel
                                .getSelected();
                        Conversation[] selection = selected
                                .toArray(new Conversation[selected.size()]);
                        ConversationSelectionEvent cse = new ConversationSelectionEvent(
                                ConversationListView.this, selection);
                        eventService.publish(cse);
                    }
                });
        ValueModel selectionHolder = new ListSelectionValueModelAdapter(table
                .getSelectionModel());
        new ListSingleSelectionGuard(selectionHolder, manualRequestExecutor);
        JScrollPane tableScrollPane = getComponentFactory().createScrollPane(
                table);
        tableScrollPane.setMinimumSize(new Dimension(100, 60));

        JPanel mainPanel = getComponentFactory()
                .createPanel(new BorderLayout());
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(filterPanel, BorderLayout.SOUTH);
        return mainPanel;
    }

    public Conversation[] getSelectedConversations() {
        EventList<Conversation> selected = conversationSelectionModel
                .getSelected();
        return selected.toArray(new Conversation[selected.size()]);
    }

    private EventList<Conversation> getConversationList() {
        return this.conversationList;
    }

    public void setConversationList(EventList<Conversation> conversationList) {
        this.conversationList = conversationList;
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

    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    private EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    private ConversationTableFactory getConversationTableFactory() {
        return conversationTableFactory;
    }

    public void setConversationTableFactory(
            ConversationTableFactory conversationTableFactory) {
        this.conversationTableFactory = conversationTableFactory;
    }

    private class ConversationFilter implements TextFilterator<Conversation> {

        public void getFilterStrings(List<String> list,
                Conversation conversation) {
            list.add(conversation.getRequestMethod());
            list.add(conversation.getRequestUri().toString());
            list.add(conversation.getResponseStatus());
            list.add(conversation.getResponseMessage());
            list.add(conversation.getSource());
            Annotation annotation = getConversationService().getAnnotation(
                    conversation.getId());
            if (annotation != null && !"".equals(annotation.getAnnotation()))
                list.add(annotation.getAnnotation());
        }

    }

    private class UriMatcher extends AbstractMatcherEditor<Conversation> {

        private Matcher<Conversation> matcher;

        private URI[] selection = new URI[0];

        public UriMatcher() {
            matcher = new Matcher<Conversation>() {
                public boolean matches(Conversation conversation) {
                    if (selection.length == 0)
                        return true;
                    for (int i = 0; i < selection.length; i++) {
                        if (conversation.getRequestUri().toString().startsWith(
                                selection[i].toString()))
                            return true;
                    }
                    return false;
                }
            };
            getEventService().subscribeStrongly(URISelectionEvent.class,
                    new SwingEventSubscriber() {
                        protected void handleEventOnEDT(EventServiceEvent evt) {
                            if (evt instanceof URISelectionEvent) {
                                URISelectionEvent use = (URISelectionEvent) evt;
                                Object source = use.getSource();
                                if (source instanceof PageComponent) {
                                    PageComponent pc = (PageComponent) source;
                                    if (pc.getContext().getPage().equals(
                                            getContext().getPage())) {
                                        selection = use.getSelection();
                                        for (int i = 0; i < selection.length; i++)
                                            if (selection.length == 0) {
                                                fireMatchNone();
                                            } else {
                                                fireChanged(matcher);
                                            }
                                    }
                                }
                            }
                        }

                    });
        }

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.matchers.AbstractMatcherEditor#getMatcher()
         */
        @Override
        public Matcher<Conversation> getMatcher() {
            return matcher;
        }

    }

    private class ManualRequestExecutor extends AbstractActionCommandExecutor {
        public void execute() {
            getContext().getPage().showView("manualRequestView");
            // This is guarded by a SingleSelection guard, so there will always be a single
            // conversation selected when this is invoked
            ManualRequestCopyEvent mrce = new ManualRequestCopyEvent(
                    ConversationListView.this, getSelectedConversations()[0]);
            getEventService().publish(mrce);
        }
    }
}
