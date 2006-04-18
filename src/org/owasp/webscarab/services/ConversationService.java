/**
 * 
 */
package org.owasp.webscarab.services;

import java.util.Collection;

import org.bushe.swing.event.EventService;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;
import org.owasp.webscarab.dao.ConversationDao;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

;

/**
 * @author rdawes
 * 
 */
public class ConversationService {

    private ConversationDao conversationDao;

    private EventService eventService = null;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public ConversationService() {
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

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void addConversation(Conversation conversation,
            ConversationSummary summary) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            getConversationDao().getId(conversation, summary);
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
            return getConversationDao().getConversationIds();
        } finally {
            readLock.unlock();
        }
    }

    public Conversation getConversation(Integer id) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getConversationDao().getConversation(id);
        } finally {
            readLock.unlock();
        }
    }

    public ConversationSummary getConversationSummary(Integer id) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getConversationDao().getConversationSummary(id);
        } finally {
            readLock.unlock();
        }
    }

    public String getConversationDescription(Integer id) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return getConversationDao().getConversationDescription(id);
        } finally {
            readLock.unlock();
        }
    }

    public void updateConversationDescription(Integer id, String description) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            getConversationDao().updateConversationDescription(id, description);
        } finally {
            writeLock.unlock();
        }
        if (eventService != null) {
            ConversationEvent evt = new ConversationEvent(this, id, description);
            eventService.publish(evt);
        }
    }

}
