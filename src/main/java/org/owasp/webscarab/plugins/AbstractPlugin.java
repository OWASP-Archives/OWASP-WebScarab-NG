/**
 * 
 */
package org.owasp.webscarab.plugins;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventSubscriber;
import org.owasp.webscarab.domain.Session;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.services.HttpService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author rdawes
 *
 */
public abstract class AbstractPlugin implements ApplicationContextAware, EventSubscriber {

    private ConversationService conversationService;
    
    private ApplicationContext applicationContext;
    
    private EventService eventService;
    
    private HttpService httpService;
    
    private Session session;
    
    protected abstract Class<?>[] getSubscribedEvents();
    
    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    protected ConversationService getConversationService() {
        if (conversationService == null)
            conversationService = (ConversationService) applicationContext
                    .getBean("conversationService");
        return conversationService;
    }

    protected ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected EventService getEventService() {
        return this.eventService;
    }

    public void setEventService(EventService eventService) {
        Class<?>[] events = getSubscribedEvents();
        if (getEventService() != null && events != null) {
            for (int i=0; i<events.length; i++)
                getEventService().unsubscribe(events[i], this);
        }
        this.eventService = eventService;
        if (getEventService() != null && events != null) {
            for (int i=0; i<events.length; i++)
                getEventService().subscribeStrongly(events[i], this);
        }
    }

    protected HttpService getHttpService() {
        return httpService;
    }
    
    /**
     * @param httpService
     *            the httpService to set
     */
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * @return the session
     */
    protected Session getSession() {
        return this.session;
    }

    /**
     * @param session
     *            the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

}
