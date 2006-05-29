/**
 * 
 */
package org.owasp.webscarab.services;

import java.util.Collection;

import org.bushe.swing.event.EventService;
import org.owasp.webscarab.dao.AnnotationDao;
import org.owasp.webscarab.dao.ConversationDao;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.ConversationSummary;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

;

/**
 * @author rdawes
 * 
 */
public class ConversationService {

	private Integer session = null;
	
    private ConversationDao conversationDao;

    private AnnotationDao annotationDao;
    
    private EventService eventService = null;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public ConversationService() {
    }

	public Integer getSession() {
		return this.session;
	}

	public void setSession(Integer session) {
		this.session = session;
	}

    /**
     * @return Returns the conversationDao.
     */
    public ConversationDao getConversationDao() {
        return conversationDao;
    }

    /**
     * @param conversationDao
     *            The conversationDao to set.
     */
    public void setConversationDao(ConversationDao conversationDao) {
        this.conversationDao = conversationDao;
    }

    public EventService getEventService() {
    	return this.eventService;
    }
    
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void addConversation(Conversation conversation,
            ConversationSummary summary) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            getConversationDao().update(session, conversation, summary);
        } catch (Exception e) {
        	e.printStackTrace();
        	return;
        } finally {
            writeLock.unlock();
        }
        if (eventService != null) {
            ConversationEvent evt = new ConversationEvent(this, conversation,
                    summary);
            eventService.publish(evt);
        }
    }

    public Collection getConversationIds() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getConversationDao().getAllIds(session);
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            readLock.unlock();
        }
    }

    public Conversation getConversation(Integer id) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getConversationDao().get(id);
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            readLock.unlock();
        }
    }

    public ConversationSummary getConversationSummary(Integer id) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getConversationDao().getSummary(id);
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            readLock.unlock();
        }
    }

	public AnnotationDao getAnnotationDao() {
		return this.annotationDao;
	}

	public void setAnnotationDao(AnnotationDao annotationDao) {
		this.annotationDao = annotationDao;
	}
	
    public Annotation getAnnotation(Integer id) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getAnnotationDao().get(id);
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            readLock.unlock();
        }
    }

    public void updateAnnotation(Annotation annotation) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            getAnnotationDao().update(annotation);
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        if (eventService != null) {
            eventService.publish("annotation", annotation);
        }
    }

}
