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
import org.owasp.webscarab.domain.Session;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author rdawes
 *
 */
public class ConversationService {

    private ConversationDao conversationDao;

    private AnnotationDao annotationDao;

    private EventService eventService = null;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

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

    public void addConversation(Session session, Conversation conversation) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            conversation = getConversationDao().add(session, conversation);
        } catch (Exception e) {
        	e.printStackTrace();
        	return;
        } finally {
            writeLock.unlock();
        }
        if (eventService != null) {
            ConversationEvent evt = new ConversationEvent(this, session, conversation);
            eventService.publish(evt);
        }
    }

    public Collection<Integer> getConversationIds(Session session) {
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
        	if (annotation.getAnnotation() != null && ! "".equals(annotation.getAnnotation())) {
        		getAnnotationDao().update(annotation);
        	} else {
        		getAnnotationDao().delete(annotation.getId());
        	}
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
