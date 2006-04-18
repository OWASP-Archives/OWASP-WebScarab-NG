package org.owasp.webscarab.dao;

import java.util.Collection;
import org.owasp.webscarab.Conversation;
import org.owasp.webscarab.ConversationSummary;

public interface ConversationDao {
	
    Collection<Integer> getConversationIds();
    
	Conversation getConversation(Integer id);
	
    ConversationSummary getConversationSummary(Integer id);
    
	void getId(Conversation conversation, ConversationSummary summary);
	
    String getConversationDescription(Integer id);
    
    void updateConversationDescription(Integer id, String description);
    
}
