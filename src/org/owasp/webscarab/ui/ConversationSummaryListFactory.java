/**
 * 
 */
package org.owasp.webscarab.ui;

import java.util.Collection;
import java.util.Iterator;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.ConversationSummary;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.services.ConversationEvent;
import org.owasp.webscarab.services.ConversationService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

/**
 * @author rdawes
 * 
 */
public class ConversationSummaryListFactory extends SwingEventSubscriber
		implements FactoryBean, ApplicationContextAware {

	private EventList<ConversationSummary> summaryList;

	private EventService eventService;

	private ConversationService conversationService;

	private ApplicationContext applicationContext = null;

	public ConversationSummaryListFactory() {
		summaryList = new SortedList<ConversationSummary>(
				new BasicEventList<ConversationSummary>());
	}

	/**
	 * @return Returns the conversationService.
	 */
	public ConversationService getConversationService() {
		if (conversationService == null)
			conversationService = (ConversationService) applicationContext
					.getBean("conversationService");
		return conversationService;
	}

	/**
	 * @param conversationService
	 *            The conversationService to set.
	 */
	public void setConversationService(ConversationService conversationService) {
		this.conversationService = conversationService;
	}

	private void updateSummaryList() {
		getSummaryList().getReadWriteLock().writeLock().lock();
		getSummaryList().clear();
		Collection ids = getConversationService().getConversationIds();
		Iterator it = ids.iterator();
		while (it.hasNext()) {
			Integer id = (Integer) it.next();
			ConversationSummary summary = getConversationService()
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
		if (getEventService() != null) {
			getEventService().unsubscribe(ConversationEvent.class, this);
			getEventService().unsubscribe(SessionEvent.class, this);
		}
		this.eventService = eventService;
		if (getEventService() != null) {
			getEventService().subscribeStrongly(ConversationEvent.class, this);
			getEventService().subscribeStrongly(SessionEvent.class, this);
		}
	}

	/**
	 * @return Returns the summaryList.
	 */
	public EventList<ConversationSummary> getSummaryList() {
		return summaryList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.owasp.webscarab.ui.SwingEventSubscriber#handleEventOnEDT(org.bushe.swing.event.EventServiceEvent)
	 */
	@Override
	protected void handleEventOnEDT(EventServiceEvent evt) {
		if (evt instanceof ConversationEvent) {
			ConversationEvent event = (ConversationEvent) evt;
			if (getSummaryList() != null) {
				getSummaryList().getReadWriteLock().writeLock().lock();
				try {
					if (event.getType() == ConversationEvent.CONVERSATION_ADDED)
						getSummaryList().add(event.getSummary());
				} finally {
					getSummaryList().getReadWriteLock().writeLock().unlock();
				}
			}
		} else if (evt instanceof SessionEvent) {
			SessionEvent event = (SessionEvent) evt;
			if (event.getType() == SessionEvent.SESSION_CHANGED) {
				updateSummaryList();
			}
		}
	}

	public Object getObject() throws Exception {
		return summaryList;
	}

	public Class getObjectType() {
		return EventList.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
