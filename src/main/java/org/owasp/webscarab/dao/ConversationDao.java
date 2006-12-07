package org.owasp.webscarab.dao;

import java.util.Collection;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.domain.Session;

public interface ConversationDao {

    Collection<Integer> getAllIds(Session session);

	Conversation get(Integer id);

	Conversation add(Session session, Conversation conversation);

}
