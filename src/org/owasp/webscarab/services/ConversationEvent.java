/**
 * 
 */
package org.owasp.webscarab.services;

import org.bushe.swing.event.EventServiceEvent;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;

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
    private Conversation conversation;
    private ConversationSummary summary;
    private String description;
    
    public ConversationEvent(Object source, Conversation conversation, ConversationSummary summary) {
        this.source = source;
        this.type = CONVERSATION_ADDED;
        this.conversation = conversation;
        this.id = conversation.getId();
        this.summary = summary;
    }
    
    public ConversationEvent(Object source, Integer id, String description) {
        this.source = source;
        this.type = DESCRIPTION_UPDATED;
        this.id = id;
        this.description = description;
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
     * @return Returns the summary.
     */
    public ConversationSummary getSummary() {
        return summary;
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
