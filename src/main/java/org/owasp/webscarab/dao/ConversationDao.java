package org.owasp.webscarab.dao;

import java.util.Collection;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.ConversationSummary;

public interface ConversationDao {
	
    Collection<Integer> getAllIds(Integer session);
    
	Conversation get(Integer id);
	
    ConversationSummary getSummary(Integer id);
    
	void update(Integer session, Conversation conversation, ConversationSummary summary);
	
}
