/**
 *
 */
package org.owasp.webscarab.services;

import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.Session;

/**
 * @author rdawes
 *
 */
public class ConversationEvent implements EventServiceEvent {

    public final static int CONVERSATION_ADDED = 0;
    public final static int DESCRIPTION_UPDATED = 1;

    private Object source;
    private int type;
    private Integer id;
    private Session session;
    private Conversation conversation;
    private String description;

    public ConversationEvent(Object source, Session session, Conversation conversation) {
        this.source = source;
        this.type = CONVERSATION_ADDED;
        this.session = session;
        this.conversation = conversation;
        this.id = conversation.getId();
    }

    public ConversationEvent(Object source, Integer id, String description) {
        this.source = source;
        this.type = DESCRIPTION_UPDATED;
        this.id = id;
        this.description = description;
    }

    /**
     * @return Returns the session.
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return Returns the conversation.
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     * @return Returns the source.
     */
    public Object getSource() {
        return source;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * @return Returns the id.
     */
    public Integer getId() {
        return id;
    }

}
