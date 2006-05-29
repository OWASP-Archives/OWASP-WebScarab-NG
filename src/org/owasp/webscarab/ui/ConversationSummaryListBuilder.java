/**
 * 
 */
package org.owasp.webscarab.ui;

import java.util.Collection;
import java.util.Iterator;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.ConversationSummary;
import org.owasp.webscarab.services.ConversationEvent;
import org.owasp.webscarab.services.ConversationService;

import ca.odell.glazedlists.EventList;

/**
 * @author rdawes
 * 
 */
public class ConversationSummaryListBuilder extends SwingEventSubscriber {

    private EventList<ConversationSummary> summaryList;

    private EventService eventService;

    private ConversationService conversationService;

    public ConversationSummaryListBuilder() {
    }

    /**
     * @return Returns the conversationService.
     */
    public ConversationService getConversationService() {
        return conversationService;
    }

    /**
     * @param conversationService
     *            The conversationService to set.
     */
    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
        if (getSummaryList() != null) {
            getSummaryList().clear();
            if (getConversationService() != null) {
                updateSummaryList();
            }

        }
    }

    private void updateSummaryList() {
        getSummaryList().getReadWriteLock().writeLock().lock();
        getSummaryList().clear();
        Collection ids = getConversationService().getConversationIds();
        Iterator it = ids.iterator();
        while (it.hasNext()) {
            Integer id = (Integer) it.next();
            ConversationSummary summary = conversationService
                    .getConversationSummary(id);
            getSummaryList().add(summary);
        }
        getSummaryList().getReadWriteLock().writeLock().unlock();
    }

    /**
     * @return Returns the eventService.
     */
    public EventService getEventService() {
        return eventService;
    }

    /**
     * @param eventService
     *            The eventService to set.
     */
    public void setEventService(EventService eventService) {
        if (getEventService() != null)
            getEventService().unsubscribe(ConversationEvent.class, this);
        this.eventService = eventService;
        if (getEventService() != null)
            getEventService().subscribeStrongly(ConversationEvent.class, this);
    }

    /**
     * @return Returns the summaryList.
     */
    public EventList<ConversationSummary> getSummaryList() {
        return summaryList;
    }

    /**
     * @param summaryList
     *            The summaryList to set.
     */
    public void setSummaryList(EventList<ConversationSummary> summaryList) {
        this.summaryList = summaryList;
        if (getSummaryList() != null && getConversationService() != null) {
            updateSummaryList();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.owasp.webscarab.ui.SwingEventSubscriber#handleEventOnEDT(org.bushe.swing.event.EventServiceEvent)
     */
    @Override
    protected void handleEventOnEDT(EventServiceEvent evt) {
        ConversationEvent event = (ConversationEvent) evt;
        if (getSummaryList() != null) {
            getSummaryList().getReadWriteLock().writeLock().lock();
            try {
                getSummaryList().add(event.getSummary());
            } finally {
                getSummaryList().getReadWriteLock().writeLock().unlock();
            }
        }
    }

}
