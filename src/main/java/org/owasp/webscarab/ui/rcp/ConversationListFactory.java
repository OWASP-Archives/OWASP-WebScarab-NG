/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.util.Collection;
import java.util.Iterator;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.Session;
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
public class ConversationListFactory extends SwingEventSubscriber
		implements FactoryBean, ApplicationContextAware {

	private EventList<Conversation> conversationList;

	private EventService eventService;

	private ConversationService conversationService;

	private ApplicationContext applicationContext = null;

	public ConversationListFactory() {
		conversationList = new SortedList<Conversation>(
				new BasicEventList<Conversation>());
	}

	/**
	 * @return Returns the conversationService.
	 */
	private ConversationService getConversationService() {
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

	private void updateSummaryList(Session session) {
		getConversationList().getReadWriteLock().writeLock().lock();
		getConversationList().clear();
		Collection<Integer> ids = getConversationService().getConversationIds(session);
		Iterator<Integer> it = ids.iterator();
		while (it.hasNext()) {
			Integer id = it.next();
			Conversation conversation = getConversationService()
					.getConversation(id);
			getConversationList().add(conversation);
		}
		getConversationList().getReadWriteLock().writeLock().unlock();
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
			getEventService().unsubscribe("annotation", this);
		}
		this.eventService = eventService;
		if (getEventService() != null) {
			getEventService().subscribeStrongly(ConversationEvent.class, this);
			getEventService().subscribeStrongly(SessionEvent.class, this);
			getEventService().subscribeStrongly("annotation", this);
		}
	}

	/**
	 * @return Returns the conversationList.
	 */
	public EventList<Conversation> getConversationList() {
		return conversationList;
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
			if (getConversationList() != null) {
				getConversationList().getReadWriteLock().writeLock().lock();
				try {
					if (event.getType() == ConversationEvent.CONVERSATION_ADDED)
						getConversationList().add(event.getConversation());
				} finally {
					getConversationList().getReadWriteLock().writeLock().unlock();
				}
			}
		} else if (evt instanceof SessionEvent) {
			SessionEvent event = (SessionEvent) evt;
			if (event.getType() == SessionEvent.SESSION_CHANGED) {
				updateSummaryList(event.getSession());
			}
		}
	}

	protected void handleEventOnEDT(String topic, Object data) {
		if (topic.equals("annotation")) {
			Annotation annotation = (Annotation) data;
			Integer id = annotation.getId();
			if (id == null) return;
			getConversationList().getReadWriteLock().writeLock().lock();
			for (int i=0; i<getConversationList().size(); i++) {
				if (getConversationList().get(i).getId().equals(id)) {
					getConversationList().set(i, getConversationList().get(i));
					break;
				}
			}
			getConversationList().getReadWriteLock().writeLock().unlock();
		}
	}

	public Object getObject() throws Exception {
		return conversationList;
	}

	@SuppressWarnings("unchecked")
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
